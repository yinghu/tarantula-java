package com.tarantula.platform;

import com.tarantula.Recoverable;

/**
 * Created by yinghu lu on 9/21/2018.
 */
public class IndexKey extends RecoverableObject implements Recoverable.Key {

    private String bucket;
    private String oid;
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
    public byte[] toByteArray() {
        return new StringBuffer(bucket).append(Recoverable.PATH_SEPARATOR).append(oid).append(Recoverable.PATH_SEPARATOR).append(index).toString().getBytes();
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
