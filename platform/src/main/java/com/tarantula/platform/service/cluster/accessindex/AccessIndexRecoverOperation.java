package com.tarantula.platform.service.cluster.accessindex;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;

import java.io.IOException;

public class AccessIndexRecoverOperation extends Operation implements PartitionAwareOperation {




    private byte[] key;
    private byte[] value;
    public AccessIndexRecoverOperation() {
    }


    public AccessIndexRecoverOperation(byte[] key) {
        this.key = key;
    }
    @Override
    public void run() throws Exception {
        AccessIndexClusterService ais = this.getService();
        value = ais.recover(key);
    }

    @Override
    public Object getResponse() {
        return this.value;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeByteArray(key);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.key = in.readByteArray();
    }
}
