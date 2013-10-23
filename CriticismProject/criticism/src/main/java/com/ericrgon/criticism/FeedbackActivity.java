package com.ericrgon.criticism;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import static com.ericrgon.criticism.S3Uploader.BUCKET_NAME;
import static com.ericrgon.criticism.S3Uploader.DESCRIPTION;
import static com.ericrgon.criticism.S3Uploader.SCREENSHOT;
import static com.ericrgon.criticism.Screenshot.SCREENSHOT_EXTRA;

public class FeedbackActivity extends Activity {

    private byte[] bytes = {};

    private CheckBox screenshotCheckbox;
    private CheckBox systemDataCheckbox;
    private EditText description;

    private String bucketName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.feedback);

        generateTitle();

        bucketName = loadBucket();

        if(getIntent().hasExtra(SCREENSHOT_EXTRA)){
            bytes = getIntent().getByteArrayExtra(SCREENSHOT_EXTRA);
            View screenshotGroup = findViewById(R.id.screenshotGroup);
            screenshotGroup.setVisibility(View.VISIBLE);
        }

        description = (EditText) findViewById(R.id.description);
        screenshotCheckbox = (CheckBox) findViewById(R.id.screenshotCheckbox);
        systemDataCheckbox = (CheckBox) findViewById(R.id.systemDataCheckbox);
    }

    private String loadBucket() {
        int feedbackId = getResources().getIdentifier("feedback_bucket","string",getPackageName());

        if(feedbackId == 0){
            throw new IllegalArgumentException(getString(R.string.bucket_name_not_found));
        }

        return getString(feedbackId);
    }

    private void generateTitle() {
        StringBuilder stringBuilder = new StringBuilder(getString(R.string.send_feedback_for));
        stringBuilder.append(" ").append(getString(getApplicationInfo().labelRes));
        setTitle(stringBuilder);

    }

    public void clickedIncludeScreenshot(View view){
        screenshotCheckbox.toggle();
    }

    public void clickedIncludeSystemData(@SuppressWarnings("UnusedParameters") View view){
        systemDataCheckbox.toggle();
    }

    public void send(View view) {
        Intent uploadIntent = new Intent(this,S3Uploader.class);
        uploadIntent.putExtra(BUCKET_NAME,bucketName);
        uploadIntent.putExtra(DESCRIPTION,description.getText().toString());
        uploadIntent.putExtra(S3Uploader.LOGS,systemDataCheckbox.isChecked());

        if(screenshotCheckbox.isChecked()){
            uploadIntent.putExtra(SCREENSHOT,bytes);
        }

        startService(uploadIntent);

        finish();

        //TODO: Check if the network connection is available.
    }

    public void cancel(View view) {
        onBackPressed();
    }
}