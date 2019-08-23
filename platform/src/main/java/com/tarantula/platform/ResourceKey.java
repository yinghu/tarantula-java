package com.tarantula.platform;

import com.tarantula.Recoverable;

/**
 * Updated by yinghu on 6/15/2018.
 */
public class ResourceKey extends RecoverableObject implements Recoverable.Key {

    public String bucket;
    public String oid;
    public String[] key;

    public ResourceKey(String bucket,String oid, String[] key){
        this.bucket = bucket;
        this.oid = oid;
        this.key = key;
    }

    public byte[] toByteArray(){
        return this.asString().getBytes();
    }
    public void fromByteArray(byte[] data){
        StringBuffer sb = new StringBuffer();
        for(byte b : data){
            sb.append((char)b);
        }
    }
    public String asString(){
        StringBuffer sb = new StringBuffer(bucket);
        sb.append(Recoverable.PATH_SEPARATOR).append(oid);
        for(String s : this.key){
            sb.append(Recoverable.PATH_SEPARATOR);
            sb.append(s);
        }
        return sb.toString();
    }
    @Override
    public String toString(){
        return asString();
    }

    /**
    @Override
    public int hashCode(){
        return this.owner.hashCode()+this.key.hashCode();
    }
    @Override
    public boolean equals(Object obj){
        ResourceKey r = (ResourceKey)obj;
        return owner.equals(r.owner)&&key.equals(r.key);
    }**/
}

