Criticism
========

An Android library that gathers users feedback and uploads an HTML report to your S3 bucket. 


Setting up
-------------------------

### Add Permisions ###
```xml
    <uses-permission android:name="android.permission.READ_LOGS"/>
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```
### Specify Bucket ###
Specify the bucket name in your strings.xml. Make sure the bucket is writable. 
```xml
    <string name="feedback_bucket">...</string>
```
### Launch Activity ###
Attach a screenshot by bundling it with the intent as an extra. Use the static method Snapshot.snap() to capture the current view.
```java
        Intent feedbackIntent = new Intent(this,FeedbackActivity.class);
        feedbackIntent.putExtra(SCREENSHOT_EXTRA, Screenshot.snap(MainActivity.this));
        startActivity(feedbackIntent);
```

