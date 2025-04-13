package com.icodesoftware.util;

import java.util.Locale;

public class OSUtil {

    public static final String X86_64 = "x86_64";
    public static final String AMD_64 = "amd64";
    public static final String AARCH64 = "aarch64";

    public static boolean windows(){
        return System.getProperty("os.name").toLowerCase(Locale.ENGLISH).startsWith("windows");
    }

    public static boolean linux(){
        return System.getProperty("os.name").toLowerCase(Locale.ENGLISH).startsWith("linux");
    }

    public static boolean macOS(){
        return System.getProperty("os.name").toLowerCase(Locale.ENGLISH).startsWith("mac");
    }

    public static String arch(){
        String arch = System.getProperty("os.arch");
        if(arch.toLowerCase(Locale.ENGLISH).startsWith(X86_64)) return X86_64;
        if(arch.toLowerCase(Locale.ENGLISH).startsWith(AMD_64)) return X86_64;
        if(arch.toLowerCase(Locale.ENGLISH).startsWith(AARCH64)) return AARCH64;
        throw new UnsupportedOperationException("os arch not supported");
    }


}
