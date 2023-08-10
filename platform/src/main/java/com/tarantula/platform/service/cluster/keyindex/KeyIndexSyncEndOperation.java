package com.tarantula.platform.service.cluster.keyindex;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.tarantula.platform.service.cluster.accessindex.AccessIndexClusterService;

import java.io.IOException;

public class KeyIndexSyncEndOperation extends Operation {

    private String syncKey;
    private boolean ret;

    public KeyIndexSyncEndOperation() {
    }

    public KeyIndexSyncEndOperation(String syncKey) {
        this.syncKey = syncKey;
    }

    @Override
    public void run() throws Exception {
        KeyIndexClusterService cds = this.getService();
        ret = cds.endSync(syncKey);
    }

    @Override
    public Object getResponse() {
        return ret;
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
