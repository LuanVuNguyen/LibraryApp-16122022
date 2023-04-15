package com.example.libraryapp.Thread;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class info_Notification {
    private Context mContext;

    public info_Notification(Context context) {
        mContext = context;
    }

    public void showMessage(String meg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(meg)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do something
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do something
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }
}
