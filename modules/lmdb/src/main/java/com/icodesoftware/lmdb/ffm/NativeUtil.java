package com.icodesoftware.lmdb.ffm;


public class NativeUtil {

    private NativeUtil(){}

    public static String storeName(String name){
        return name;
    }

    public static String storeName(String name,String label){
        return name+"#"+label;
    }


}
