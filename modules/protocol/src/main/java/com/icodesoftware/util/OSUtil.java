package com.icodesoftware.util;

public class OSUtil {

    public static boolean windows(){
        return System.getProperty("os.name").toLowerCase().startsWith("windows");
    }
}
