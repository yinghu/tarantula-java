package com.tarantula.platform.service.cluster.accessindex;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;

import java.io.IOException;

public class AccessIndexRecoverOperation extends Operation implements PartitionAwareOperation {


    private String source;

    private byte[] key;
    private byte[] value;
    public AccessIndexRecoverOperation() {
    }


    public AccessIndexRecoverOperation(String source,byte[] key) {
        this.source = source;
        this.key = key;
    }
    @Override
    public void run() throws Exception {
        AccessIndexClusterService ais = this.getService();
        value = ais.recover(source,key);
    }

    @Override
    public Object getResponse() {
        return this.value;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(source);
        out.writeByteArray(key);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.source = in.readUTF();
        this.key = in.readByteArray();
    }
}
