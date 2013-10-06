package com.ericrgon;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private Bitmap getBitmapFromView(Activity activity){
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        Bitmap bitmap = Bitmap.createBitmap(displayMetrics.widthPixels,displayMetrics.heightPixels, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        activity.getWindow().getDecorView().draw(canvas);
        return bitmap;
    }

    public void snap(View view){
        Bitmap screenshot = getBitmapFromView(this);
        try {
            File destination = new File(getExternalFilesDir(null),"example.png");
            Log.d("Snapshot", destination.getAbsolutePath());
            FileOutputStream fileOutputStream = new FileOutputStream(destination);
            screenshot.compress(Bitmap.CompressFormat.PNG,100,fileOutputStream);
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent feedbackIntent = new Intent(this,FeedbackActivity.class);
        startActivity(feedbackIntent);
    }

}
