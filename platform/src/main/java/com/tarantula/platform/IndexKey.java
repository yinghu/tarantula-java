package com.tarantula.platform;

import com.tarantula.Recoverable;

/**
 * Created by yinghu lu on 8/23/19
 */
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
        NaturalKey r = (NaturalKey)obj;
        return this.asString().equals(r.asString());
    }
}
