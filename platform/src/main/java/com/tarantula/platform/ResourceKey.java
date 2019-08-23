package com.tarantula.platform;

import com.tarantula.Recoverable;

/**
 * Updated by yinghu on 8/23/19
 */
public class ResourceKey extends RecoverableObject implements Recoverable.Key {

    public String[] key;

    public ResourceKey(String bucket,String oid, String[] key){
        this.bucket = bucket;
        this.oid = oid;
        this.key = key;
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


    @Override
    public int hashCode(){
        return this.asString().hashCode();
    }
    @Override
    public boolean equals(Object obj){
        ResourceKey r = (ResourceKey)obj;
        return this.asString().equals(r.asString());
    }
}

