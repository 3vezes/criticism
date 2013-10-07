package com.ericrgon;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.ericrgon.s3.S3Uploader;

public class FeedbackActivity extends Activity {

    private final String BUCKET_NAME = "Feedback";

    private Button send;
    private Button cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.feedback);

        StringBuilder stringBuilder = new StringBuilder(getString(R.string.send_feedback_for));
        stringBuilder.append(" ").append(getString(getApplicationInfo().labelRes));
        setTitle(stringBuilder);

        ImageView screenShot = (ImageView) findViewById(R.id.screenshot);
        final byte[] bytes = getIntent().getByteArrayExtra("bitmap");
        final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        screenShot.setImageBitmap(bitmap);

        send = (Button) findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                S3Uploader s3Uploader = new S3Uploader(FeedbackActivity.this,BUCKET_NAME,bytes);
                s3Uploader.upload();
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
}