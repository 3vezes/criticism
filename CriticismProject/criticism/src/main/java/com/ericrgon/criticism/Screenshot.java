package com.ericrgon.criticism;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.DisplayMetrics;

import java.io.ByteArrayOutputStream;


public class Screenshot {

    public static final String SCREENSHOT_EXTRA = "screenshot";

    public static byte[] snap(Activity activity){
        Bitmap screenshot = getBitmapFromView(activity);
        byte[] bytes = compressBitmap(screenshot);
        screenshot.recycle();
        return bytes;
    }

    private static byte[] compressBitmap(Bitmap bitmap){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private static Bitmap getBitmapFromView(Activity activity){
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        Bitmap bitmap = Bitmap.createBitmap(displayMetrics.widthPixels,displayMetrics.heightPixels, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        activity.getWindow().getDecorView().draw(canvas);
        return bitmap;
    }

}
