package com.ericrgon;

import android.app.Activity;
import android.os.Bundle;


public class FeedbackActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StringBuilder stringBuilder = new StringBuilder(getString(R.string.send_feedback_for));
        stringBuilder.append(" ").append(getString(getApplicationInfo().labelRes));
        setTitle(stringBuilder);

        setContentView(R.layout.feedback);
    }
}
