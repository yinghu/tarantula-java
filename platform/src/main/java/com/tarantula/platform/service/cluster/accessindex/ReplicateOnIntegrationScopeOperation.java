package com.tarantula.platform.service.cluster.accessindex;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

/**
 * updated by yinghu lu on 7/10/2020.
 */
public class ReplicateOnIntegrationScopeOperation extends Operation {

    private int partition;
    private byte[] key;
    private byte[] value;

    public ReplicateOnIntegrationScopeOperation() {
    }


    public ReplicateOnIntegrationScopeOperation( int partition,byte[] key, byte[] value) {
        this.partition = partition;
        this.key = key;
        this.value = value;
    }

    @Override
    public void run() throws Exception {
        AccessIndexClusterService cis = this.getService();
        cis.replicate(partition,key,value);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeInt(partition);
        out.writeByteArray(key);
        out.writeByteArray(value);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        partition = in.readInt();
        key = in.readByteArray();
        value = in.readByteArray();
    }
}
