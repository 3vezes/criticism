package com.ericrgon.criticism;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Logs {

    public static String getLogString() throws IOException {
        Process process = Runtime.getRuntime().exec("logcat -d");
        InputStream inputStream = process.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder result = new StringBuilder();

        String line;
        while ((line = bufferedReader.readLine()) != null){
            result.append(line).append("\n");
        }

        return result.toString();
    }

}
