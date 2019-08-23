package com.tarantula.platform;


import com.tarantula.Recoverable;


/**
 * updated by yinghu lu on 8/23/2019.
 */
public class AssociateKey extends RecoverableObject implements Recoverable.Key {

    public AssociateKey(String bucket, String oid, String suffix){
        this.bucket = bucket;
        this.oid = oid;
        this.label = suffix;
    }

    @Override
    public String asString() {
        return new StringBuffer(bucket).append(Recoverable.PATH_SEPARATOR).append(oid).append(Recoverable.PATH_SEPARATOR).append(label).toString();
    }
    @Override
    public int hashCode(){
        return this.asString().hashCode();
    }
    @Override
    public boolean equals(Object obj){
        AssociateKey r = (AssociateKey)obj;
        return this.asString().equals(r.asString());
    }
}
