package com.tarantula.platform.service.cluster.accessindex;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

public class ReplicateOnIntegrationScopeOperation extends Operation {

    private String nodeName;
    private byte[] key;
    private byte[] value;

    public ReplicateOnIntegrationScopeOperation() {
    }


    public ReplicateOnIntegrationScopeOperation(String nodeName,byte[] key, byte[] value) {
        this.nodeName = nodeName;

        this.key = key;
        this.value = value;
    }

    @Override
    public void run() throws Exception {
        AccessIndexClusterService cis = this.getService();
        cis.replicate(nodeName,key,value);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(nodeName);
        out.writeByteArray(key);
        out.writeByteArray(value);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        nodeName = in.readUTF();
        key = in.readByteArray();
        value = in.readByteArray();
    }
}
