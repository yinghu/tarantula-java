package com.tarantula.platform.service.cluster.recover;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

public class ReplicateOnDataScopeOperation extends Operation {

    private String source;

    private byte[] key;
    private byte[] value;


    public ReplicateOnDataScopeOperation() {
    }


    public ReplicateOnDataScopeOperation(String source, byte[] key, byte[] value) {
        this.source = source;
        this.key = key;
        this.value = value;
    }

    @Override
    public void run() throws Exception {
        ClusterRecoverService cis = this.getService();
        cis.replicate(source,key,value);
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
        source = in.readUTF();
        key = in.readByteArray();
        value = in.readByteArray();
    }
}
