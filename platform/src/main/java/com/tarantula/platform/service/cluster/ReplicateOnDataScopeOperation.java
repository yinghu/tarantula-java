package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

/**
 * updated by yinghu lu on 7/10/2020.
 */
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
        out.writeUTF(this.source);
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
