package com.ericrgon.criticism;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.ericrgon.criticism.s3.S3Uploader;

public class FeedbackActivity extends Activity {

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

        final byte[] bytes = getIntent().getByteArrayExtra("bitmap");
        final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        description = (EditText) findViewById(R.id.description);
        screenshotCheckbox = (CheckBox) findViewById(R.id.screenshotCheckbox);
        systemDataCheckbox = (CheckBox) findViewById(R.id.systemDataCheckbox);

        send = (Button) findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressDialog progressDialog = new SendingDialog(FeedbackActivity.this);

                final S3Uploader s3Uploader = new S3Uploader(FeedbackActivity.this,bucketName){
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        progressDialog.show();
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        progressDialog.dismiss();

                        Toast.makeText(FeedbackActivity.this,getString(R.string.thank_you),Toast.LENGTH_LONG).show();

                        finish();
                        super.onPostExecute(aVoid);
                    }
                };

                if(screenshotCheckbox.isChecked()){
                    s3Uploader.setDescription(description.getText().toString());
                }

                s3Uploader.setSendLogs(systemDataCheckbox.isChecked());

                s3Uploader.setScreenshot(bytes);

                s3Uploader.execute();
            }
        });

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
}