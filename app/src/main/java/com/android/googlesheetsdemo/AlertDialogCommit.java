package com.android.googlesheetsdemo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Created by OPTIMUSDOM ubuntu151 on 12/11/15.
 */
public class AlertDialogCommit extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Save to Cloud ? (" + EmployeesLunchActivity.getTotalUsersForLunch() + ")")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        CommitClass commitClass = new CommitClass(getContext());
                        commitClass.setupInit();
                        commitClass.makeApiCall();

                    }
                })
                .setNegativeButton("Cancel", null

                );
        return builder.create();
    }
}