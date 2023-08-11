package com.tarantula.platform.service.cluster.accessindex;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;

import java.io.IOException;

public class AccessIndexLoadOperation extends Operation implements PartitionAwareOperation {


    private int partition;

    private byte[] key;
    private byte[] value;
    public AccessIndexLoadOperation() {
    }


    public AccessIndexLoadOperation(int partition, byte[] key) {
        this.partition = partition;
        this.key = key;
    }
    @Override
    public void run() throws Exception {
        AccessIndexClusterService ais = this.getService();
        value = ais.recover(partition,key);
    }

    @Override
    public Object getResponse() {
        return this.value;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeInt(partition);
        out.writeByteArray(key);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.partition = in.readInt();
        this.key = in.readByteArray();
    }
}
