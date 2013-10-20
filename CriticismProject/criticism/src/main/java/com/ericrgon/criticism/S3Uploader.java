package com.ericrgon.criticism;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.Grant;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


class S3Uploader extends AsyncTask<Void,Void,Boolean>{

    private final Context context;

    private final String bucketName;
    private final String reportName;
    private final File cacheDir;

    private byte[] screenshot = {};
    private String description;
    private boolean sendLogs;

    private final String applicationName;
    private String applicationVersion = "Unknown Version";

    private static final Grant GRANT = new Grant(GroupGrantee.AllUsers, Permission.FullControl);
    private static final AccessControlList ACCESS_CONTROL_LIST = new AccessControlList();

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
        } catch (PackageManager.NameNotFoundException ignored) {}

        ACCESS_CONTROL_LIST.grantAllPermissions(GRANT);
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

    @Override
    protected Boolean doInBackground(Void... voids) {
        boolean isUploadSuccessful = false;

        File report = null;
        try {
            AmazonS3Client amazonS3Client = new AmazonS3Client();

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(screenshot);
            ObjectMetadata metadata = new ObjectMetadata();

            PutObjectRequest putObjectRequest = new PutObjectRequest(getPath(), "screenshot.png", byteArrayInputStream, metadata);
            putObjectRequest.setAccessControlList(ACCESS_CONTROL_LIST);
            amazonS3Client.putObject(putObjectRequest);

            ReportGenerator reportGenerator = new ReportGenerator(context,applicationName,applicationVersion,description,cacheDir,sendLogs);

            report = reportGenerator.generate(context);

            //Upload report
            PutObjectRequest index = new PutObjectRequest(getPath(),"index.html",report);
            index.setAccessControlList(ACCESS_CONTROL_LIST);
            amazonS3Client.putObject(index);

            sendResource(amazonS3Client, R.raw.bootstrap,"bootstrap.css","text/css");

            isUploadSuccessful = true;

        } catch (FileNotFoundException ignored) {
        } catch (AmazonClientException ignored){
        } catch (IOException ignored) {
        } finally {
            if(report != null){
                report.delete();
            }
        }

        return isUploadSuccessful;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSendLogs(boolean sendLogs) {
        this.sendLogs = sendLogs;
    }

    public boolean isSendLogs() {
        return sendLogs;
    }

    public void setScreenshot(byte[] screenshot) {
        this.screenshot = screenshot;
    }

    public byte[] getScreenshot() {
        return screenshot;
    }
}