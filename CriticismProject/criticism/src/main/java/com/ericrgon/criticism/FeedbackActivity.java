package com.ericrgon.criticism;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import static com.ericrgon.criticism.Screenshot.SCREENSHOT_EXTRA;

public class FeedbackActivity extends Activity {

    byte[] bytes = {};

    private Button send;
    private Button cancel;

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

        send = (Button) findViewById(R.id.send);

        cancel = (Button) findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
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

    public void clickedIncludeSystemData(View view){
        systemDataCheckbox.toggle();
    }

    public void send(View view) {
        final ProgressDialog progressDialog = new SendingDialog(FeedbackActivity.this);

        final S3Uploader s3Uploader = new S3Uploader(FeedbackActivity.this,bucketName){
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog.show();
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                progressDialog.dismiss();
            }

            @Override
            protected void onPostExecute(Boolean isUploadSuccessful) {
                super.onPostExecute(isUploadSuccessful);
                progressDialog.dismiss();

                if(isUploadSuccessful){


                    Toast.makeText(FeedbackActivity.this,getString(R.string.thank_you),Toast.LENGTH_LONG).show();

                    finish();
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(FeedbackActivity.this)
                            .setMessage(getString(R.string.failed_to_upload))
                            .setPositiveButton(getString(R.string.retry),new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    send(null);
                                }
                            })
                            .setNegativeButton(getString(R.string.cancel),new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //Exit without sending the report.
                                    finish();
                                }
                            }).create();

                    alertDialog.show();
                }
            }
        };

        s3Uploader.setDescription(description.getText().toString());

        if(screenshotCheckbox.isChecked()){
            s3Uploader.setScreenshot(bytes);
        }

        s3Uploader.setSendLogs(systemDataCheckbox.isChecked());

        s3Uploader.execute();
    }
}