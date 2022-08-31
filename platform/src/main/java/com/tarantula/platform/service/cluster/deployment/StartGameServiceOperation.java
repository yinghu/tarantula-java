package com.tarantula.platform.service.cluster.deployment;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

public class StartGameServiceOperation extends Operation {


    private String gameClusterKey;
    public StartGameServiceOperation() {
    }


    public StartGameServiceOperation(String gameClusterKey) {
        this.gameClusterKey = gameClusterKey;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        cds.onStartGameService(gameClusterKey);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(gameClusterKey);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.gameClusterKey = in.readUTF();
    }
}
