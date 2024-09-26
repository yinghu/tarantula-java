package com.icodesoftware.util;

public class StringUtil {

    public static String toUnderScore(String propertyName){
        String f = propertyName.substring(0,1);
        return propertyName.replaceFirst(f,"_"+f.toLowerCase());
    }

    public static String toUpCaseFirst(String name){
        return name.substring(0,1).toUpperCase()+name.substring(1);
    }

}
