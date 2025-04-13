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
            return "/home/yinghu/"+arch+"-windows-gnu.dll";
        }
        if(OSUtil.linux()){
            return "/home/yinghu/"+arch+"-linux-gnu.dll";
        }
        if(OSUtil.macOS()){
            return "/home/yinghu/"+arch+"-macos-none.dll";
        }
        throw new UnsupportedOperationException("Native lib not supported");
    }

}
