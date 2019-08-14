package com.tarantula.platform;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.Recoverable;

import java.io.IOException;

/**
 * Created by yinghu lu on 9/21/2018.
 */
public class IndexKey implements Recoverable.Key {

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
    public String getPartitionKey() {
        return bucket;
    }

    @Override
    public int getFactoryId() {
        return 0;
    }

    @Override
    public int getClassId() {
        return 0;
    }

    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {

    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {

    }
}
