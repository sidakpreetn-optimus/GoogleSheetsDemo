package com.android.googlesheetsdemo;

import android.app.ProgressDialog;
import android.content.Context;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by OPTIMUSDOM ubuntu151 on 17/11/15.
 */
public class Utils {

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final String PREF_APP = "GoogleAccountPreferences";
    static final String PREF_ACCOUNT_NAME = "accountName";
    static final String[] SCOPES = {"https://www.googleapis.com/auth/drive", "https://www.googleapis.com/auth/spreadsheets"};

    public static ProgressDialog getProgressDialog(Context context, String message) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(message);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    public static void saveToLocal(ArrayList<EmployeeModel> list, Context context) {
        EmployeeDAO employeeDAO = new EmployeeDAO(context);
        employeeDAO.open();
        employeeDAO.emptyTable();
        employeeDAO.createFromArrayList(list);
        employeeDAO.close();
    }

    public static boolean ifDatabaseExists(Context context) {
        File database = context.getDatabasePath(DBHelper.DATABASE_NAME);

        if (!database.exists()) {
            return false;
        } else {
            return true;
        }
    }

    public static void deleteDatabase(Context context) {
        context.deleteDatabase(DBHelper.DATABASE_NAME);
    }

    /**
     * Extend the given HttpRequestInitializer (usually a credentials object)
     * with additional initialize() instructions.
     *
     * @param requestInitializer the initializer to copy and adjust; typically
     *                           a credential object.
     * @return an initializer with an extended read timeout.
     */
    static HttpRequestInitializer setHttpTimeout(
            final HttpRequestInitializer requestInitializer) {
        return new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest httpRequest)
                    throws java.io.IOException {
                requestInitializer.initialize(httpRequest);
                // This allows the API to call (and avoid timing out on)
                // functions that take up to 6 minutes to complete (the maximum
                // allowed script run time), plus a little overhead.
                httpRequest.setReadTimeout(380000);
            }
        };
    }
}