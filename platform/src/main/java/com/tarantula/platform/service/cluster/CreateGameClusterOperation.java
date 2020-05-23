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


    private GameCluster result;

    private String name;
    private String plan;
    public CreateGameClusterOperation() {
    }


    public CreateGameClusterOperation(String name,String plan) {
        this.name = name;
        this.plan = plan;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        this.result = cds.createGameCluster(this.name,this.plan);
    }

    @Override
    public Object getResponse() {
        return this.result;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(name);
        out.writeUTF(plan);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.name=in.readUTF();
        this.plan = in.readUTF();
    }
}
