package com.ericrgon.criticism;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ReportGenerator {

    private static final String TAG_REGEX = ".*\\{[^)]*\\}.*";
    private static final Pattern TAG_PATTERN = Pattern.compile(TAG_REGEX);

    private static final String APP_TAG = "{app_name}";
    private static final String VERSION_TAG = "{version}";
    private static final String DESCRIPTION_TAG = "{description}";
    private static final String LOGS_TAG = "{logs}";

    private final String applicationName;
    private final String applicationVersion;
    private final String description;
    private final File cacheDir;

    public ReportGenerator(String applicationName, String applicationVersion, String description, File cacheDir) {
        this.applicationName = applicationName;
        this.applicationVersion = applicationVersion;
        this.description = description;
        this.cacheDir = cacheDir;
    }

    public File generate(Context context) throws IOException {
        InputStream reportIndex = context.getResources().openRawResource(R.raw.index);
        return writeReport(reportIndex,cacheDir);
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
}
