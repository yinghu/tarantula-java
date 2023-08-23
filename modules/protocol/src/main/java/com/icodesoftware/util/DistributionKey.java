package com.icodesoftware.util;


import com.icodesoftware.Recoverable;

public class DistributionKey implements Recoverable.Key{

    private String bucket;
    private String oid;

    public DistributionKey(String bucket, String oid){
        this.bucket = bucket;
        this.oid = oid;
    }
    public byte[] asBinary(){
        return null;
    }
    public String asString(){
        if(bucket==null||oid==null){
            return null;
        }
        return new StringBuffer(bucket).append(Recoverable.PATH_SEPARATOR).append(oid).toString();
    }
    @Override
    public String toString(){
        return asString();
    }
    @Override
    public boolean equals(Object obj){
        return this.asString().equals(((DistributionKey)obj).asString());
    }
    @Override
    public int hashCode(){
        return this.asString().hashCode();
    }
}