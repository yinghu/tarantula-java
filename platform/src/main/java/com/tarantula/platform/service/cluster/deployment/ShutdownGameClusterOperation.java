package com.tarantula.platform.service.cluster.deployment;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

/**
 * created by yinghu lu on 5/15/2020.
 */
public class ShutdownGameClusterOperation extends Operation {


    private String gameClusterkey;
    public ShutdownGameClusterOperation() {
    }


    public ShutdownGameClusterOperation(String gameClusterkey) {
        this.gameClusterkey = gameClusterkey;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        cds.shutdownGameCluster(gameClusterkey);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(gameClusterkey);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.gameClusterkey = in.readUTF();
    }
}
