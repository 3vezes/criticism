package com.ericrgon.criticism;


import android.app.ProgressDialog;
import android.content.Context;

public class SendingDialog extends ProgressDialog{

    public SendingDialog(Context context) {
        super(context);
        setIndeterminate(true);
        setProgressStyle(ProgressDialog.STYLE_SPINNER);
        setMessage(context.getString(R.string.sending_report));
    }
}
