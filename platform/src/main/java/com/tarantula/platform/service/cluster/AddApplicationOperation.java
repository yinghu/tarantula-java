package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.tarantula.Descriptor;
import com.tarantula.platform.DeploymentDescriptor;
import com.tarantula.platform.util.SystemUtil;

import java.io.IOException;

/**
 * updated by yinghu lu on 5/29/2019.
 */
public class AddApplicationOperation extends Operation {


    private Descriptor descriptor;

    private String result;

    public AddApplicationOperation() {
    }


    public AddApplicationOperation(Descriptor lobby) {
        this.descriptor = lobby;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        this.result = cds.addApplication(this.descriptor);
    }

    @Override
    public Object getResponse() {
        return this.result;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeByteArray(SystemUtil.toJson(this.descriptor.toMap()));
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.descriptor = new DeploymentDescriptor();
        this.descriptor.fromMap(SystemUtil.toMap(in.readByteArray()));
    }
}
