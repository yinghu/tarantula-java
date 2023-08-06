package com.tarantula.platform.service.cluster.keyindex;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;
import com.icodesoftware.service.KeyIndex;

import java.io.IOException;


public class KeyIndexLookupOperation extends Operation implements PartitionAwareOperation {

    private byte[] keyIndex;
    private byte[] key;

    public KeyIndexLookupOperation() {
    }

    public KeyIndexLookupOperation(byte[] pending) {
        this.key = pending;
    }
    @Override
    public void run() throws Exception {
        KeyIndexClusterService ais = this.getService();
        this.keyIndex = ais.get(key);
    }

    @Override
    public Object getResponse() {
        return this.keyIndex;
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
