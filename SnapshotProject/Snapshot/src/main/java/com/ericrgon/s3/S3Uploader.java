package com.ericrgon.s3;

import android.content.Context;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.Grant;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.ericrgon.R;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.List;


public class S3Uploader {

    private final Context context;

    private final String bucketName;
    private final String reportName;
    private final File cacheDir;
    private final byte[] screenshot;

    private static final Grant GRANT = new Grant(GroupGrantee.AllUsers, Permission.FullControl);
    private static final AccessControlList ACCESS_CONTROL_LIST = new AccessControlList();

    private static final List<String> TAGS = Lists.newArrayList("{app_name},{version},{description},{logs}");

    private static final String TAG_REGEX = ".*\\{[^)]*\\}.*";

    private static final String APP_TAG = "{app_name}";
    private static final String VERSION_TAG = "{version}";
    private static final String DESCRIPTION_TAG = "{description}";
    private static final String LOGS_TAG = "{logs}";

    public S3Uploader(Context context,String bucketName,byte[] screenshot) {
        this.context = context;
        this.bucketName = bucketName;
        this.reportName = new Date().toString();
        this.screenshot = screenshot;
        this.cacheDir = context.getCacheDir();
        ACCESS_CONTROL_LIST.grantAllPermissions(GRANT);
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
                    File cacheDirectory = context.getCacheDir();

                    InputStream reportIndex = context.getResources().openRawResource(R.raw.index);
                    report = writeReport(reportIndex, cacheDirectory);

                    //Upload report
                    PutObjectRequest index = new PutObjectRequest(getPath(),"index.html",report);
                    index.setAccessControlList(ACCESS_CONTROL_LIST);
                    amazonS3Client.putObject(index);

                    sendResource(amazonS3Client, R.raw.bootstrap,"bootstrap.css");

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

    private void sendResource(AmazonS3Client client,int resourceId,String destinationFileName) throws IOException {
        InputStream resourceStream = context.getResources().openRawResource(resourceId);
        PutObjectRequest request = new PutObjectRequest(getPath(),destinationFileName,resourceStream,new ObjectMetadata());
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

        String current = "";
        while ((current = bufferedReader.readLine()) != null){
            //Check if the current line contains a tag.
            if(current.matches(TAG_REGEX)){
                Iterable<String> tags = Splitter.on(TAG_REGEX).split(current);
                for(String tag : tags){
                    if (tag.contains(APP_TAG)) {
                        current = current.replace(APP_TAG,"Application");
                    } else if(tag.contains(VERSION_TAG)){
                        current = current.replace(VERSION_TAG,"Version.");
                    } else if(tag.contains(DESCRIPTION_TAG)){
                        current = current.replace(DESCRIPTION_TAG,"My own description");
                    } else if(tag.contains(LOGS_TAG)){
                        current = current.replace(LOGS_TAG,"My Logs");
                    }
                }
            }
            stringWriter.write(current + "\n");
        }

        stringWriter.close();

        return cachedIdex;
    }
}