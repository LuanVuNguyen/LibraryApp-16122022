package com.example.libraryapp.Thread;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import com.example.libraryapp.activities.resgister_product;

public class Notificaiton {
    private Context mContext;

    public Notificaiton(Context context) {
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
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

}
