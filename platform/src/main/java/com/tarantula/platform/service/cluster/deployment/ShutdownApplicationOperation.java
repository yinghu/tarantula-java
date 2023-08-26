package com.tarantula.platform.service.cluster.deployment;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

public class ShutdownApplicationOperation extends Operation {

    private String typeId;
    private long applicationkey;
    public ShutdownApplicationOperation() {
    }


    public ShutdownApplicationOperation(String typeId, long applicationkey) {
        this.typeId = typeId;
        this.applicationkey = applicationkey;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        cds.onShutdownApplication(typeId,applicationkey);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(typeId);
        out.writeLong(applicationkey);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.typeId = in.readUTF();
        this.applicationkey = in.readLong();
    }
}
