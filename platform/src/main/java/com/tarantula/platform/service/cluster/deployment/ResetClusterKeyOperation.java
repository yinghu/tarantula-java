package com.tarantula.platform.service.cluster.deployment;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

public class ResetClusterKeyOperation extends Operation {

    public ResetClusterKeyOperation() {
    }


    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        cds.resetClusterKey();
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);

    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);

    }
}
