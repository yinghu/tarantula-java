package com.icodesoftware.util;

public class StringUtil {

    public static String toUnderScore(String propertyName){
        String f = propertyName.substring(0,1);
        return propertyName.replaceFirst(f,"_"+f.toLowerCase());
    }

}
