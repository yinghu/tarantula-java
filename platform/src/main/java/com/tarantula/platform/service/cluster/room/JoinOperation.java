package com.tarantula.platform.service.cluster.room;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;

import java.io.IOException;

public class JoinOperation extends Operation implements PartitionAwareOperation {

    private byte[] payload;
    @Override
    public void run() throws Exception {
        RoomClusterService ais = this.getService();
        //accessIndex = ais.get(accessKey);
        payload = "join".getBytes();
    }

    @Override
    public Object getResponse() {
        return payload;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
    }
}
