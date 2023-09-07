package com.tarantula.platform.service.cluster.deployment;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;


public class OnCreateGameClusterOperation extends Operation {

    private long akey;


    public OnCreateGameClusterOperation() {
    }


    public OnCreateGameClusterOperation(long akey) {
        this.akey = akey;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cis = this.getService();
        cis.onCreateGameCluster(akey);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeLong(akey);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.akey = in.readLong();
    }
}
