package com.tarantula.platform.service.cluster.deployment;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

public class ShutdownApplicationOperation extends Operation {

    private String typeId;
    private long applicationId;
    public ShutdownApplicationOperation() {
    }


    public ShutdownApplicationOperation(String typeId, long applicationId) {
        this.typeId = typeId;
        this.applicationId = applicationId;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        cds.onShutdownApplication(typeId,applicationId);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(typeId);
        out.writeLong(applicationId);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.typeId = in.readUTF();
        this.applicationId = in.readLong();
    }
}
