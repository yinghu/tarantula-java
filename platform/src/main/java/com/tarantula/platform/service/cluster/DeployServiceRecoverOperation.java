package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

/**
 * created by yinghu lu on 6/4/2019.
 */
public class DeployServiceRecoverOperation extends Operation {

    //private Batch result;
    private String destination;
    private String registerId;
    private boolean fullBackup;
    public DeployServiceRecoverOperation() {
    }
    public DeployServiceRecoverOperation(String destination,String registerId,boolean fullBackup) {
        this.destination = destination;
        this.registerId = registerId;
        this.fullBackup = fullBackup;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        cds.recover(destination,registerId,fullBackup);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(this.destination);
        out.writeUTF(this.registerId);
        out.writeBoolean(fullBackup);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.destination = in.readUTF();
        this.registerId = in.readUTF();
        this.fullBackup = in.readBoolean();
    }
}
