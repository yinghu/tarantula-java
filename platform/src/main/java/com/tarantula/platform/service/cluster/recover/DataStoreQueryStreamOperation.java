package com.tarantula.platform.service.cluster.recover;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;


public class DataStoreQueryStreamOperation extends Operation {

    private String source;
    private byte[] key;
    private byte[] value;
    public DataStoreQueryStreamOperation() {
    }


    public DataStoreQueryStreamOperation(String source, byte[] key,byte[] value) {

        this.source = source;
        this.key = key;
        this.value = value;
    }
    @Override
    public void run() throws Exception {
        ClusterRecoverService cds = this.getService();
        cds.query(source,key,value);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(source);
        out.writeByteArray(key);
        out.writeByteArray(value);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.source = in.readUTF();
        this.key = in.readByteArray();
        this.value = in.readByteArray();
    }
}
