package com.icodesoftware.lmdb.ffm;


import com.icodesoftware.util.OSUtil;

public class NativeUtil {

    private NativeUtil(){}

    public static String storeName(String name){
        return name;
    }

    public static String storeName(String name,String label){
        return name+"#"+label;
    }

    public static String libName(){
        String arch = OSUtil.arch();
        if(OSUtil.windows()){
            return arch+"-windows-gnu.dll";
        }
        if(OSUtil.linux()){
            return arch+"-linux-gnu.so";
        }
        if(OSUtil.macOS()){
            return arch+"-macos-none.so";
        }
        throw new UnsupportedOperationException("Native lib not supported");
    }

}
