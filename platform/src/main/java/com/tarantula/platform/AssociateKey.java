package com.tarantula.platform;


import com.tarantula.Recoverable;


/**
 * Created by yinghu lu on 9/21/2018.
 */
public class AssociateKey extends RecoverableObject implements Recoverable.Key {

    private String bucket;
    private String oid;
    private String suffix;

    public AssociateKey(String bucket, String oid, String suffix){
        this.bucket = bucket;
        this.oid = oid;
        this.suffix = suffix;
    }

    @Override
    public String asString() {
        return new StringBuffer(bucket).append(Recoverable.PATH_SEPARATOR).append(oid).append(Recoverable.PATH_SEPARATOR).append(suffix).toString();
    }

    @Override
    public byte[] toByteArray() {
        return asString().getBytes();
    }

    @Override
    public void fromByteArray(byte[] data) {

    }


    @Override
    public int getFactoryId() {
        return 0;
    }

    @Override
    public int getClassId() {
        return 0;
    }


}
