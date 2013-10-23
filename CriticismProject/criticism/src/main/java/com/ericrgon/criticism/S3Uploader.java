package com.ericrgon.criticism;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

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


public class S3Uploader extends IntentService{

    public static final String BUCKET_NAME = "bucket_name";
    public static final String DESCRIPTION = "description";
    public static final String SCREENSHOT = "screenshot";
    public static final String LOGS = "logs";

    private Context context;

    private File cacheDir;

    private String applicationName;
    private String applicationVersion = "Unknown Version";

    private static final Grant GRANT = new Grant(GroupGrantee.AllUsers, Permission.FullControl);
    private static final AccessControlList ACCESS_CONTROL_LIST = new AccessControlList();


    public S3Uploader() {
        super("S3Uploader");
    }

    private void sendResource(AmazonS3Client client,int resourceId,String destinationFileName,String contentType,String bucketName,String reportName) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);

        InputStream resourceStream = context.getResources().openRawResource(resourceId);
        PutObjectRequest request = new PutObjectRequest(getPath(bucketName,reportName),destinationFileName,resourceStream,metadata);
        request.setAccessControlList(ACCESS_CONTROL_LIST);
        client.putObject(request);
        resourceStream.close();
    }

    private String getPath(String bucketName,String reportName){
        return bucketName + "/" + reportName;
    }

    /**
     * Report name is structured as com.example-hh:mm-dd-mm-yyyy
     *
     * @return
     * @param applicationContext
     */
    private String generateReportName(Context applicationContext) {
        String packageName = applicationContext.getPackageName();
        Date currentDate = new Date();
        SimpleDateFormat formattedDate = new SimpleDateFormat("HH:mm-dd-MM-yyyy");
        return packageName + "-" + formattedDate.format(currentDate);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        init();

        String bucketName = intent.getStringExtra(BUCKET_NAME);
        byte[] screenshot = intent.getByteArrayExtra(SCREENSHOT);
        String description = intent.getStringExtra(DESCRIPTION);
        boolean sendLogs = intent.getBooleanExtra(LOGS,false);

        String reportName = generateReportName(getApplicationContext());

        File report = null;
        try {
            AmazonS3Client amazonS3Client = new AmazonS3Client();

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(screenshot);
            ObjectMetadata metadata = new ObjectMetadata();

            PutObjectRequest putObjectRequest = new PutObjectRequest(getPath(bucketName,reportName), "screenshot.png", byteArrayInputStream, metadata);
            putObjectRequest.setAccessControlList(ACCESS_CONTROL_LIST);
            amazonS3Client.putObject(putObjectRequest);

            ReportGenerator reportGenerator = new ReportGenerator(context,applicationName,applicationVersion,description,cacheDir,sendLogs);

            report = reportGenerator.generate(context);

            //Upload report
            PutObjectRequest index = new PutObjectRequest(getPath(bucketName,reportName),"index.html",report);
            index.setAccessControlList(ACCESS_CONTROL_LIST);
            amazonS3Client.putObject(index);

            sendResource(amazonS3Client, R.raw.bootstrap,"bootstrap.css","text/css",bucketName,reportName);

        } catch (FileNotFoundException ignored) {
            ignored.printStackTrace();
        } catch (AmazonClientException ignored){
            ignored.printStackTrace();
        } catch (IOException ignored){
            ignored.printStackTrace();
        } finally {
            if(report != null){
                report.delete();
            }
        }
    }

    private void init() {
        this.context = getApplication();
        this.cacheDir = context.getCacheDir();
        applicationName = context.getString(context.getApplicationInfo().labelRes);
        PackageInfo pInfo = null;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            applicationVersion = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException ignored) {}

        ACCESS_CONTROL_LIST.grantAllPermissions(GRANT);
    }
}