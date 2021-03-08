package com.tarantula.platform.service.cluster.deployment;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

/**
 * updated by yinghu lu on 7/10/2020.
 */
public class  DataSyncOperation extends Operation {

    private String akey;


    public DataSyncOperation() {
    }


    public DataSyncOperation(String akey) {
        this.akey = akey;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cis = this.getService();
        cis.sync(akey);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(akey);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.akey = in.readUTF();
    }
}
