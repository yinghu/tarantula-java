package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.icodesoftware.service.Batch;

import java.io.IOException;

/**
 * updated by yinghu lu on 5/29/2019.
 */
public class DeployServiceGetOperation extends Operation {


    private String  batchId;
    private int count;

    private Batch payload;

    public DeployServiceGetOperation() {
    }


    public DeployServiceGetOperation(String  batchId,int count) {
        this.batchId = batchId;
        this.count = count;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        payload = cds.query(batchId,count);
    }

    @Override
    public Object getResponse() {
        return this.payload;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(batchId);
        out.writeInt(count);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.batchId = in.readUTF();
        this.count = in.readInt();
    }
}
