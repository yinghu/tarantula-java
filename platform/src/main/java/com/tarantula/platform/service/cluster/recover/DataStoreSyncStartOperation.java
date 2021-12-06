package com.tarantula.platform.service.cluster.recover;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

public class DataStoreSyncStartOperation extends Operation {


    private String memberId;
    private String source;
    private String syncKey;
    private int totalPartitions;
    public DataStoreSyncStartOperation() {
    }


    public DataStoreSyncStartOperation(String memberId,String source,String syncKey) {
        this.memberId = memberId;
        this.source = source;
        this.syncKey = syncKey;
    }
    @Override
    public void run() throws Exception {
        ClusterRecoverService cds = this.getService();
        totalPartitions = cds.syncStart(memberId,source,syncKey);
    }

    @Override
    public Object getResponse() {
        return totalPartitions;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(memberId);
        out.writeUTF(source);
        out.writeUTF(syncKey);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.memberId = in.readUTF();
        this.source = in.readUTF();
        this.syncKey = in.readUTF();
    }
}
