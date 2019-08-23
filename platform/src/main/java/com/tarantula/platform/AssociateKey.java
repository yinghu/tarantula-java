package com.tarantula.platform;


import com.tarantula.Recoverable;


/**
 * updated by yinghu lu on 8/23/2019.
 */
public class AssociateKey extends RecoverableObject implements Recoverable.Key {

    //private String bucket;
    //private String oid;
    //private String suffix;

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
    public byte[] toByteArray() {
        return asString().getBytes();
    }

    @Override
    public void fromByteArray(byte[] data) {

    }

}
