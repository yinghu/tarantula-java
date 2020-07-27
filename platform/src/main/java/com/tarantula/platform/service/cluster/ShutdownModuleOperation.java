package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

/**
 * created by yinghu lu on 5/15/2020.
 */
public class ShutdownModuleOperation extends Operation {


    private String typeId;
    public ShutdownModuleOperation() {
    }


    public ShutdownModuleOperation(String typeId) {
        this.typeId = typeId;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        cds.shutdownModule(typeId);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(typeId);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.typeId = in.readUTF();
    }
}
