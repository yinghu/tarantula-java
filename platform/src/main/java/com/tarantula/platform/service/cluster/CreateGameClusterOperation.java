package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.icodesoftware.service.GameCluster;

import java.io.IOException;

/**
 * created by yinghu lu on 5/15/2020.
 */
public class CreateGameClusterOperation extends Operation {


    private GameCluster result;
    private String owner;
    private String name;
    public CreateGameClusterOperation() {
    }


    public CreateGameClusterOperation(String owner,String name) {
        this.owner = owner;
        this.name = name;

    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        this.result = cds.createGameCluster(this.owner,this.name);
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
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.owner = in.readUTF();
        this.name=in.readUTF();
    }
}
