package com.tarantula.platform;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.RecoverableObject;

public class IndexKey extends RecoverableObject implements Recoverable.Key {

    private int index;

    public IndexKey(String bucket, String oid, int  index){
        this.bucket = bucket;
        this.oid = oid;
        this.index = index;
    }

    @Override
    public String asString() {
        return new StringBuffer(bucket).append(Recoverable.PATH_SEPARATOR).append(oid).append(Recoverable.PATH_SEPARATOR).append(index).toString();
    }
    @Override
    public int hashCode(){
        return this.asString().hashCode();
    }
    @Override
    public boolean equals(Object obj){
        IndexKey r = (IndexKey)obj;
        return this.asString().equals(r.asString());
    }
}
