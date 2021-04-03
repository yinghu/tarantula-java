package com.tarantula.platform.service.cluster.accessindex;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

public class AccessIndexSyncStartOperation extends Operation {


    private String memberId;
    private int totalPartitions;
    public AccessIndexSyncStartOperation() {
    }


    public AccessIndexSyncStartOperation(String memberId) {
        this.memberId = memberId;
    }
    @Override
    public void run() throws Exception {
        AccessIndexClusterService cds = this.getService();
        totalPartitions = cds.sync(memberId);
    }

    @Override
    public Object getResponse() {
        return totalPartitions;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(memberId);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.memberId = in.readUTF();
    }
}
