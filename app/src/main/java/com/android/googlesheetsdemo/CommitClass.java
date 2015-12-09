package com.android.googlesheetsdemo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.script.model.ExecutionRequest;
import com.google.api.services.script.model.Operation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by OPTIMUSDOM ubuntu151 on 17/11/15.
 */
public class CommitClass {

    private Context context;
    GoogleAccountCredential mCredential;
    ProgressDialog mProgress;

    CommitClass(Context context) {
        this.context = context;
    }

    void setupInit() {

        mProgress = Utils.getProgressDialog(context, "Loading...");
        SharedPreferences settings = context.getSharedPreferences(Utils.PREF_APP, Context.MODE_PRIVATE);
        mCredential = GoogleAccountCredential.usingOAuth2(
                context, Arrays.asList(Utils.SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(Utils.PREF_ACCOUNT_NAME, null));
    }

    void makeApiCall() {
        if (isDeviceOnline()) {
            new MakeRequestTask(mCredential).execute();
        } else {
            Toast.makeText(context, "No network connection available.", Toast.LENGTH_LONG).show();
        }
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private class MakeRequestTask extends AsyncTask<Void, Void, Integer> {
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
        protected Integer doInBackground(Void... params) {
            try {
                return postDataToApi();
            } catch (Exception e) {
                mProgress.dismiss();
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        private Integer postDataToApi()
                throws IOException, GoogleAuthException {

            ArrayList<Object> parameterList = EmployeesLunchActivity.getLunchUsersList();

            String scriptId = "McLYqTshBgAhX97yJv41K_dYBFPxhiUIF";

            ExecutionRequest request = new ExecutionRequest()
                    .setFunction("addParameter")
                    .setParameters(parameterList)
                    .setDevMode(true);

            Operation op =
                    mService.scripts().run(scriptId, request).execute();

            if (op.getError() != null) {
                throw new IOException(getScriptError(op));
            }
            if (op.getResponse() != null) {

                return 1;
            }
            return 0;
        }

        private String getScriptError(Operation op) {
            if (op.getError() == null) {
                return null;
            }

            Map<String, Object> detail = op.getError().getDetails().get(0);
            List<Map<String, Object>> stacktrace =
                    (List<Map<String, Object>>) detail.get("scriptStackTraceElements");

            java.lang.StringBuilder sb =
                    new StringBuilder("\nScript error message: ");
            sb.append(detail.get("errorMessage"));

            if (stacktrace != null) {

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
        protected void onPostExecute(Integer result) {
            mProgress.hide();
            if (result == 1) {
                Toast.makeText(context, "Successful", Toast.LENGTH_LONG).show();
                Utils.deleteDatabase(context);
                context.startActivity(new Intent(context, MainActivity.class));
            } else {
                Toast.makeText(context, "Final Error", Toast.LENGTH_LONG).show();
            }
        }
    }
}
