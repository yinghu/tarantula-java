package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.tarantula.platform.presence.GameCluster;

import java.io.IOException;

/**
 * created by yinghu lu on 5/15/2020.
 */
public class CreateGameClusterOperation extends Operation {


    private GameCluster descriptor;

    private boolean result;

    public CreateGameClusterOperation() {
    }


    public CreateGameClusterOperation(GameCluster lobby) {
        this.descriptor = lobby;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        this.result = cds.createGameCluster(this.descriptor);
    }

    @Override
    public Object getResponse() {
        return this.result;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(descriptor.name());
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.descriptor = new GameCluster();
        this.descriptor.name(in.readUTF());
    }
}
