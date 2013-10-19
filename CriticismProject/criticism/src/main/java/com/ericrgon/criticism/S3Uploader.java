package com.ericrgon.criticism;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.Grant;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class S3Uploader extends AsyncTask<Void,Void,Void>{

    private final Context context;

    private final String bucketName;
    private final String reportName;
    private final File cacheDir;

    private byte[] screenshot;
    private String description;
    private boolean sendLogs;

    private final String applicationName;
    private String applicationVersion = "Unknown Version";

    private static final Grant GRANT = new Grant(GroupGrantee.AllUsers, Permission.FullControl);
    private static final AccessControlList ACCESS_CONTROL_LIST = new AccessControlList();

    private static final String TAG_REGEX = ".*\\{[^)]*\\}.*";
    private static final Pattern TAG_PATTERN = Pattern.compile(TAG_REGEX);

    private static final String APP_TAG = "{app_name}";
    private static final String VERSION_TAG = "{version}";
    private static final String DESCRIPTION_TAG = "{description}";
    private static final String LOGS_TAG = "{logs}";

    public S3Uploader(Context context,String bucketName) {
        this.context = context;
        this.bucketName = bucketName;
        this.reportName = generateReportName(context);
        this.cacheDir = context.getCacheDir();

        applicationName = context.getString(context.getApplicationInfo().labelRes);

        PackageInfo pInfo = null;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            applicationVersion = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {}

        ACCESS_CONTROL_LIST.grantAllPermissions(GRANT);
    }

    /**
     * Report name is structured as com.example-hh:mm-dd-mm-yyyy
     *
     * @param context
     * @return
     */
    private String generateReportName(Context context) {
        String packageName = context.getPackageName();
        Date currentDate = new Date();
        SimpleDateFormat formattedDate = new SimpleDateFormat("HH:mm-dd-MM-yyyy");
        return packageName + "-" + formattedDate.format(currentDate);
    }

    public void upload(){
        new Thread(){
            @Override
            public void run() {
                super.run();
                AmazonS3Client amazonS3Client = new AmazonS3Client();
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(screenshot);
                ObjectMetadata metadata = new ObjectMetadata();

                PutObjectRequest putObjectRequest = new PutObjectRequest(getPath(), "screenshot.png", byteArrayInputStream, metadata);
                putObjectRequest.setAccessControlList(ACCESS_CONTROL_LIST);
                amazonS3Client.putObject(putObjectRequest);

                File report = null;
                try {
                    InputStream reportIndex = context.getResources().openRawResource(R.raw.index);
                    report = writeReport(reportIndex, cacheDir);

                    //Upload report
                    PutObjectRequest index = new PutObjectRequest(getPath(),"index.html",report);
                    index.setAccessControlList(ACCESS_CONTROL_LIST);
                    amazonS3Client.putObject(index);

                    sendResource(amazonS3Client, R.raw.bootstrap,"bootstrap.css","text/css");

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if(report != null){
                        report.delete();
                    }
                }
            }
        }.start();
    }

    private void sendResource(AmazonS3Client client,int resourceId,String destinationFileName,String contentType) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);

        InputStream resourceStream = context.getResources().openRawResource(resourceId);
        PutObjectRequest request = new PutObjectRequest(getPath(),destinationFileName,resourceStream,metadata);
        request.setAccessControlList(ACCESS_CONTROL_LIST);
        client.putObject(request);
        resourceStream.close();
    }

    private String getPath(){
        return bucketName + "/" + reportName;
    }

    private File writeReport(InputStream sourceStream,File cacheDir) throws IOException {
        File cachedIdex = cacheDir.createTempFile("index",".html");
        FileOutputStream fileOutputStream = new FileOutputStream(cachedIdex);

        InputStreamReader inputStreamReader = new InputStreamReader(sourceStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        OutputStreamWriter stringWriter = new OutputStreamWriter(fileOutputStream);

        String current;
        while ((current = bufferedReader.readLine()) != null){
            //Check if the current line contains a tag.
            if(current.matches(TAG_REGEX)){
                Matcher matcher = TAG_PATTERN.matcher(current);
                while (matcher.find()){
                    String tag = matcher.group(0);
                    if (tag.contains(APP_TAG)) {
                        current = current.replace(APP_TAG,applicationName);

                    } else if(tag.contains(VERSION_TAG)){
                        current = current.replace(VERSION_TAG,applicationVersion);
                    } else if(tag.contains(DESCRIPTION_TAG)){
                        current = current.replace(DESCRIPTION_TAG,description);
                    } else if(tag.contains(LOGS_TAG)){
                        String logsString = Logs.getLogString();
                        current = current.replace(LOGS_TAG,logsString);
                    }

                }
            }
            stringWriter.write(current + "\n");
        }

        stringWriter.close();

        return cachedIdex;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isSendLogs() {
        return sendLogs;
    }

    public void setSendLogs(boolean sendLogs) {
        this.sendLogs = sendLogs;
    }

    public byte[] getScreenshot() {
        return screenshot;
    }

    public void setScreenshot(byte[] screenshot) {
        this.screenshot = screenshot;
    }

    @Override
    protected Void doInBackground(Void... voids) {

        upload();

        return null;
    }
}