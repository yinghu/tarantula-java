package com.tarantula.platform.service.cluster.recover;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

public class DataStoreSyncEndOperation extends Operation {

    private String syncKey;

    public DataStoreSyncEndOperation() {
    }

    public DataStoreSyncEndOperation(String syncKey) {
        this.syncKey = syncKey;
    }

    @Override
    public void run() throws Exception {
        ClusterRecoverService cds = this.getService();
        cds.syncEnd(syncKey);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(syncKey);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        syncKey = in.readUTF();
    }
}
