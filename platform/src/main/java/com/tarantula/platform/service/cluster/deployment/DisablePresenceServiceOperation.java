package com.tarantula.platform.service.cluster.deployment;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

public class DisablePresenceServiceOperation extends Operation {


    private String clusterNameSuffix;

    public DisablePresenceServiceOperation() {
    }

    public DisablePresenceServiceOperation(String clusterNameSuffix) {
        this.clusterNameSuffix = clusterNameSuffix;
    }

    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        cds.disablePresenceService(clusterNameSuffix);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(clusterNameSuffix);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        clusterNameSuffix = in.readUTF();
    }
}
