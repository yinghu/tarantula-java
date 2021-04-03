package com.tarantula.platform.service.cluster.recover;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

public class LoadOperation extends Operation {

    private String source;
    private byte[] key;
    private byte[] value;

    public LoadOperation() {
    }
    public LoadOperation(String source, byte[] key) {
        this.source = source;
        this.key = key;
    }
    @Override
    public void run() throws Exception {
        ClusterRecoverService cis = this.getService();
        value = cis.load(source,key);
    }

    @Override
    public Object getResponse() {
        return this.value;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(this.source);
        out.writeByteArray(key);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.source = in.readUTF();
        this.key = in.readByteArray();
    }
}
