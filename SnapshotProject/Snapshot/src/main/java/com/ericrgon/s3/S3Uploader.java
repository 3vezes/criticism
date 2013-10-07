package com.ericrgon.s3;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.Grant;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.util.Date;


public class S3Uploader {

    private final String bucketName;
    private final String reportName;
    private final byte[] screenshot;


    public S3Uploader(String bucketName,byte[] screenshot) {
        this.bucketName = bucketName;
        this.reportName = new Date().toString();
        this.screenshot = screenshot;
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
                Grant grant = new Grant(GroupGrantee.AllUsers, Permission.FullControl);
                AccessControlList accessControlList = new AccessControlList();
                accessControlList.grantAllPermissions(grant);
                putObjectRequest.setAccessControlList(accessControlList);

                amazonS3Client.putObject(putObjectRequest);

            }
        }.start();
    }

    private String getPath(){
        StringBuilder stringBuilder = new StringBuilder(bucketName);
        stringBuilder.append("/").append(reportName);
        return stringBuilder.toString();
    }
}
