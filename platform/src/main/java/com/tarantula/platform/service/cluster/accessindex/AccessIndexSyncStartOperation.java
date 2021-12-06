package com.tarantula.platform.service.cluster.accessindex;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.tarantula.platform.service.cluster.recover.ClusterRecoverService;

import java.io.IOException;

public class AccessIndexSyncStartOperation extends Operation {


    private String memberId;
    private int source;
    private String syncKey;
    private int totalPartitions;
    public AccessIndexSyncStartOperation() {
    }


    public AccessIndexSyncStartOperation(String memberId, int source, String syncKey) {
        this.memberId = memberId;
        this.source = source;
        this.syncKey = syncKey;
    }
    @Override
    public void run() throws Exception {
        AccessIndexClusterService cds = this.getService();
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
        out.writeInt(source);
        out.writeUTF(syncKey);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.memberId = in.readUTF();
        this.source = in.readInt();
        this.syncKey = in.readUTF();
    }
}
