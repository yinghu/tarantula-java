package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

/**
 * created by yinghu lu on 5/15/2020.
 */
public class LaunchModuleOperation extends Operation {


    private String typeId;
    public LaunchModuleOperation() {
    }


    public LaunchModuleOperation(String typeId) {
        this.typeId = typeId;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        cds.launchModule(typeId);
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
