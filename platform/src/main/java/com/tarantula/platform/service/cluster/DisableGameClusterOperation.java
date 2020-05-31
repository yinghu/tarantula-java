package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

/**
 * created by yinghu lu on 5/31/2020.
 */
public class DisableGameClusterOperation extends Operation {


    private String gameClusterId;

    private boolean result;

    public DisableGameClusterOperation() {
    }


    public DisableGameClusterOperation(String gameClusterId) {
        this.gameClusterId = gameClusterId;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        this.result = cds.disableGameCluster(this.gameClusterId);
    }

    @Override
    public Object getResponse() {
        return this.result;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(gameClusterId);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.gameClusterId = in.readUTF();
    }
}
