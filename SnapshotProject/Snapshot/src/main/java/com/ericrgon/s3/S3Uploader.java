package com.ericrgon.s3;

import android.content.Context;

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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;


public class S3Uploader {

    private final String bucketName;
    private final String reportName;
    private final File cacheDir;
    private final byte[] screenshot;

    private static final Grant GRANT = new Grant(GroupGrantee.AllUsers, Permission.FullControl);
    private static final AccessControlList ACCESS_CONTROL_LIST = new AccessControlList();

    public S3Uploader(Context context,String bucketName,byte[] screenshot) {
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

                try {
                    FileWriter fileWriter = new FileWriter(getLogFile());
                    Process process = Runtime.getRuntime().exec("logcat -d");
                    BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(process.getInputStream()));

                    String line;
                    while ((line = bufferedReader.readLine()) != null)
                    {
                        fileWriter.write(line + "\n");
                    }

                    fileWriter.close();

                    PutObjectRequest logObjectRequest = new PutObjectRequest(getPath(),"logs.log",new File(getLogFile()));
                    logObjectRequest.setAccessControlList(ACCESS_CONTROL_LIST);
                    amazonS3Client.putObject(logObjectRequest);

                }
                catch (IOException e) {}

            }
        }.start();
    }

    private String getPath(){
        return bucketName + "/" + reportName;
    }

    private String getLogFile(){
        return cacheDir.getAbsolutePath() + "/" + "logs.log";
    }
}
