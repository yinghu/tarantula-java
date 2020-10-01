package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.icodesoftware.service.Batch;

import java.io.IOException;

/**
 * updated by yinghu lu on 5/29/2019.
 */
public class DeployServiceQueryOperation extends Operation {


    private int registryId;
    private String[] params;

    private Batch payload;

    public DeployServiceQueryOperation() {
    }


    public DeployServiceQueryOperation(int registryId,String[] params) {
        this.registryId = registryId;
        this.params = params;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        payload = cds.query(registryId,params);
    }

    @Override
    public Object getResponse() {
        return this.payload;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeInt(registryId);
        out.writeUTFArray(params);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.registryId = in.readInt();
        this.params = in.readUTFArray();
    }
}
