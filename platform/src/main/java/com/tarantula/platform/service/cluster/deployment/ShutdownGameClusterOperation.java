package com.tarantula.platform.service.cluster.deployment;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

public class ShutdownGameClusterOperation extends Operation {


    private long gameClusterId;
    public ShutdownGameClusterOperation() {
    }


    public ShutdownGameClusterOperation(long gameClusterId) {
        this.gameClusterId = gameClusterId;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        cds.onShutdownGameCluster(gameClusterId);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeLong(gameClusterId);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.gameClusterId = in.readLong();
    }
}
