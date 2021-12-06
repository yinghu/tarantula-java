package com.tarantula.platform.service.cluster.accessindex;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.tarantula.platform.service.cluster.recover.ClusterRecoverService;

import java.io.IOException;

public class AccessIndexSyncEndOperation extends Operation {

    private String syncKey;

    public AccessIndexSyncEndOperation() {
    }

    public AccessIndexSyncEndOperation(String syncKey) {
        this.syncKey = syncKey;
    }

    @Override
    public void run() throws Exception {
        AccessIndexClusterService cds = this.getService();
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
