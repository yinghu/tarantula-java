package com.tarantula.platform.service.cluster.deployment;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;


import java.io.IOException;

public class PingConnectionOperation extends Operation {


    private String  typeId;
    private String serverId;


    public PingConnectionOperation() {
    }


    public PingConnectionOperation(String typeId, String serverId) {
        this.typeId = typeId;
        this.serverId = serverId;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        cds.pingConnection(typeId,serverId);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(typeId);
        out.writeUTF(serverId);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.typeId = in.readUTF();
        this.serverId = in.readUTF();
    }
}
