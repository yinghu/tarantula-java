package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

/**
 * Created by yinghu lu on 7/25/2020
 */
public class AckServerPushEventOperation extends Operation {

    private String serverId;
    public AckServerPushEventOperation() {
    }
    public AckServerPushEventOperation(String serverId) {
        this.serverId = serverId;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        cds.ackServerPushEvent(serverId);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(serverId);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        serverId = in.readUTF();
    }
}
