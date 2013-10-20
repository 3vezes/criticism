package com.ericrgon;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.ericrgon.criticism.FeedbackActivity;
import com.ericrgon.criticism.Screenshot;

import static com.ericrgon.criticism.Screenshot.SCREENSHOT_EXTRA;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()){
            case R.id.send_feedback:
                snap();
                return true;
            default:
                return super.onMenuItemSelected(featureId, item);
        }
    }

    public void snap(){
        Intent feedbackIntent = new Intent(this,FeedbackActivity.class);
        feedbackIntent.putExtra(SCREENSHOT_EXTRA, Screenshot.snap(MainActivity.this));
        startActivity(feedbackIntent);
    }
}
