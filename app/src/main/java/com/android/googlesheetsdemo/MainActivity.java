package com.android.googlesheetsdemo;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ArrayMap;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.script.model.ExecutionRequest;
import com.google.api.services.script.model.Operation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    GoogleAccountCredential mCredential;
    ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_main);

        mProgress = Utils.getProgressDialog(this, "Loading Employees List ...");

        // Initialize credentials and service object.
        SharedPreferences settings = getSharedPreferences(Utils.PREF_APP, Context.MODE_PRIVATE);
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(Utils.SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(Utils.PREF_ACCOUNT_NAME, null));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isGooglePlayServicesAvailable()) {
            if (Utils.ifDatabaseExists(this)) {
                setDataToList(EmployeeDAO.getArrayList(this));
            } else {
                refreshResults();
            }
        } else {
            Toast.makeText(this, "Google Play Services required: " +
                    "after installing, close and relaunch this app.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Utils.REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    isGooglePlayServicesAvailable();
                }
                break;
            case Utils.REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        mCredential.setSelectedAccountName(accountName);
                        SharedPreferences settings =
                                getSharedPreferences(Utils.PREF_APP, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(Utils.PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Account unspecified.", Toast.LENGTH_LONG).show();
                }
                break;
            case Utils.REQUEST_AUTHORIZATION:
                if (resultCode != RESULT_OK) {
                    chooseAccount();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void refreshResults() {
        if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            if (isDeviceOnline()) {
                new MakeRequestTask(mCredential).execute();
            } else {
                Toast.makeText(this, "No network connection available.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void chooseAccount() {
        startActivityForResult(
                mCredential.newChooseAccountIntent(), Utils.REQUEST_ACCOUNT_PICKER);
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private boolean isGooglePlayServicesAvailable() {
        final int connectionStatusCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        } else if (connectionStatusCode != ConnectionResult.SUCCESS) {
            return false;
        }
        return true;
    }

    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                connectionStatusCode,
                MainActivity.this,
                Utils.REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    private class MakeRequestTask extends AsyncTask<Void, Void, ArrayList<EmployeeModel>> {
        private com.google.api.services.script.Script mService = null;
        private Exception mLastError = null;

        public MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.script.Script.Builder(
                    transport, jsonFactory, Utils.setHttpTimeout(credential))
                    .setApplicationName("Google Apps Script Execution API Android Quickstart")
                    .build();
        }

        @Override
        protected ArrayList<EmployeeModel> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        private ArrayList<EmployeeModel> getDataFromApi()
                throws IOException, GoogleAuthException {
            // ID of the script to call. Acquire this from the Apps Script editor,
            // under Publish > Deploy as API executable.
            String scriptId = "McLYqTshBgAhX97yJv41K_dYBFPxhiUIF";

            // Create an execution request object.
            ExecutionRequest request = new ExecutionRequest()
                    .setFunction("getData")
                    .setDevMode(true);

            // Make the request.
            Operation op =
                    mService.scripts().run(scriptId, request).execute();


            ArrayList<ArrayMap<String, String>> tempList = null;

            // Print results of request.
            if (op.getError() != null) {
                throw new IOException(getScriptError(op));
            }
            if (op.getResponse() != null &&
                    op.getResponse().get("result") != null) {
                // The result provided by the API needs to be cast into
                // the correct type, based upon what types the Apps Script
                // function returns. Here, the function returns an Apps
                // Script Object with String keys and values, so must be
                // cast into a Java Map (folderSet).
                tempList = (ArrayList<ArrayMap<String, String>>) op.getResponse().get("result");

            }
            ArrayList<EmployeeModel> employeesList = new ArrayList();
            for (int i = 1; i < tempList.size(); i++) {
                EmployeeModel model = new EmployeeModel();
                model.setEmpCode(Integer.parseInt(tempList.get(i).get("EmpCode")));
                model.setMailId(tempList.get(i).get("OptimusMailId"));
                model.setName(tempList.get(i).get("Name"));
                employeesList.add(model);
            }

            return employeesList;
        }

        private String getScriptError(Operation op) {
            if (op.getError() == null) {
                return null;
            }

            // Extract the first (and only) set of error details and cast as a Map.
            // The values of this map are the script's 'errorMessage' and
            // 'errorType', and an array of stack trace elements (which also need to
            // be cast as Maps).
            Map<String, Object> detail = op.getError().getDetails().get(0);
            List<Map<String, Object>> stacktrace =
                    (List<Map<String, Object>>) detail.get("scriptStackTraceElements");

            java.lang.StringBuilder sb =
                    new StringBuilder("\nScript error message: ");
            sb.append(detail.get("errorMessage"));

            if (stacktrace != null) {
                // There may not be a stacktrace if the script didn't start
                // executing.
                sb.append("\nScript error stacktrace:");
                for (Map<String, Object> elem : stacktrace) {
                    sb.append("\n  ");
                    sb.append(elem.get("function"));
                    sb.append(":");
                    sb.append(elem.get("lineNumber"));
                }
            }
            sb.append("\n");
            return sb.toString();
        }


        @Override
        protected void onPreExecute() {
            mProgress.show();
        }

        @Override
        protected void onPostExecute(ArrayList<EmployeeModel> output) {
            mProgress.hide();
            if (output == null || output.size() == 0) {
                Toast.makeText(getApplicationContext(), "No results returned.", Toast.LENGTH_LONG).show();
            } else {
                Utils.saveToLocal(output, getApplicationContext());
                setDataToList(output);
            }
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            Utils.REQUEST_AUTHORIZATION);
                } else {
                    Toast.makeText(getApplicationContext(), "The following error occurred:\n" + mLastError.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Request cancelled." + mLastError.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setDataToList(ArrayList<EmployeeModel> employeeList) {
        Intent i = new Intent(this, EmployeesLunchActivity.class);
        i.putParcelableArrayListExtra("EmployeesList", employeeList);
        startActivity(i);
    }
}