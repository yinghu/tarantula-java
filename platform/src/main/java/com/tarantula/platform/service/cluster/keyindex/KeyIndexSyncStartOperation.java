package com.tarantula.platform.service.cluster.keyindex;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

public class KeyIndexSyncStartOperation extends Operation {


    private String memberId;
    private String syncKey;
    private boolean started;
    public KeyIndexSyncStartOperation() {
    }


    public KeyIndexSyncStartOperation(String memberId, String syncKey) {
        this.memberId = memberId;
        this.syncKey = syncKey;
    }
    @Override
    public void run() throws Exception {
        KeyIndexClusterService cds = this.getService();
        started = cds.startSync(memberId,syncKey);
    }

    @Override
    public Object getResponse() {
        return started;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(memberId);
        out.writeUTF(syncKey);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.memberId = in.readUTF();
        this.syncKey = in.readUTF();
    }
}
