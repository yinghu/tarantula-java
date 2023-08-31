package com.tarantula.platform.service.cluster.deployment;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;


public class LaunchApplicationOperation extends Operation {

    private String typeId;
    private String applicationKey;
    public LaunchApplicationOperation() {
    }


    public LaunchApplicationOperation(String typeId,String applicationKey) {
        this.typeId = typeId;
        this.applicationKey = applicationKey;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        cds.onLaunchApplication(typeId,applicationKey);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(typeId);
        out.writeUTF(applicationKey);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.typeId = in.readUTF();
        this.applicationKey = in.readUTF();
    }
}
