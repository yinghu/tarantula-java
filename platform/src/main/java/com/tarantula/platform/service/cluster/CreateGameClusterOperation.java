package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.tarantula.admin.GameCluster;

import java.io.IOException;

/**
 * created by yinghu lu on 5/15/2020.
 */
public class CreateGameClusterOperation extends Operation {


    private GameCluster result;
    private String owner;
    private String name;
    private String plan;
    public CreateGameClusterOperation() {
    }


    public CreateGameClusterOperation(String owner,String name,String plan) {
        this.owner = owner;
        this.name = name;
        this.plan = plan;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        this.result = cds.createGameCluster(this.owner,this.name,this.plan);
    }

    @Override
    public Object getResponse() {
        return this.result;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(owner);
        out.writeUTF(name);
        out.writeUTF(plan);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.owner = in.readUTF();
        this.name=in.readUTF();
        this.plan = in.readUTF();
    }
}
