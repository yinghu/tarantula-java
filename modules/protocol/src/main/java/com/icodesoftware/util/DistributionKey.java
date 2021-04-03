package com.icodesoftware.util;


import com.icodesoftware.Recoverable;

public class DistributionKey extends RecoverableObject implements Recoverable.Key{

    private String bucket;
    private String oid;

    public DistributionKey(String bucket, String oid){
        this.bucket = bucket;
        this.oid = oid;
    }

    public String asString(){
        if(bucket!=null&&oid!=null){
            return new StringBuffer(bucket).append(Recoverable.PATH_SEPARATOR).append(oid).toString();
        }
        else{
            return null;
        }
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