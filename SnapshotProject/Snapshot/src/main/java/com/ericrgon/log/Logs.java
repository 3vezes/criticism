package com.ericrgon.log;

import java.io.IOException;
import java.io.InputStream;

public class Logs {

    public static InputStream getLogStream() throws IOException {
        Process process = Runtime.getRuntime().exec("logcat -d");
        return process.getInputStream();
    }

}
