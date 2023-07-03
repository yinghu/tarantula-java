package com.tarantula.platform.service.cluster.recover;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

public class DeleteOperation extends Operation {

    private String source;
    private byte[] key;

    public DeleteOperation() {
    }


    public DeleteOperation(String source, byte[] key) {
        this.source = source;
        this.key = key;
    }
    @Override
    public void run() throws Exception {
        ClusterRecoverService cis = this.getService();
        cis.delete(source,key);
    }

    @Override
    public Object getResponse() {
        return null;
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
