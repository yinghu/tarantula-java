package com.tarantula.platform.service.cluster.deployment;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.tarantula.platform.GameCluster;

import java.io.IOException;

/**
 * created by yinghu lu on 5/15/2020.
 */
public class CreateGameClusterOperation extends Operation {


    private GameCluster result;
    private String owner;
    private String name;
    private String mode;
    private boolean tournamentEnabled;
    private String publishingId;
    public CreateGameClusterOperation() {
    }


    public CreateGameClusterOperation(String owner,String name,String mode,boolean tournamentEnabled,String publishingId) {
        this.owner = owner;
        this.name = name;
        this.mode = mode;
        this.tournamentEnabled = tournamentEnabled;
        this.publishingId = publishingId;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        this.result = cds.createGameCluster(this.owner,this.name,this.mode,this.tournamentEnabled,this.publishingId);
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
        out.writeUTF(mode);
        out.writeBoolean(tournamentEnabled);
        out.writeUTF(publishingId);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.owner = in.readUTF();
        this.name=in.readUTF();
        this.mode = in.readUTF();
        this.tournamentEnabled = in.readBoolean();
        this.publishingId = in.readUTF();
    }
}
