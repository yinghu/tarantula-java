package com.icodesoftware.etcd;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.Base64Util;

public class WatchKey implements Recoverable.Key{

    public static String PREFIX = "tarantula#";

    private String prefix;
    private String key;

    public WatchKey(String prefix,String key){
        this.prefix = prefix!=null?  prefix : PREFIX;
        this.key = key;
    }

    public String asString(){
        return Base64Util.toBase64String(asBinary());
    }

    @Override
    public String toString(){
        return key;
    }

    @Override
    public byte[] asBinary() {
        return new StringBuffer(prefix!=null? prefix : PREFIX).append(key).toString().getBytes();
    }

    @Override
    public int hashCode(){
        return this.key.hashCode();
    }

    @Override
    public boolean equals(Object obj){
        WatchKey r = (WatchKey)obj;
        return key.equals(r.key);
    }

}
