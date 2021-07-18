package com.tarantula.platform.service.cluster.deployment;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.icodesoftware.Descriptor;
import com.tarantula.platform.DeploymentDescriptor;

import java.io.IOException;

public class AddApplicationOperation extends Operation {


    private Descriptor descriptor;
    private String postSetup;
    private String configName;

    private String result;

    public AddApplicationOperation() {
    }


    public AddApplicationOperation(Descriptor lobby,String postSetup,String configName) {
        this.descriptor = lobby;
        this.postSetup = postSetup;
        this.configName = configName;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        this.result = cds.addApplication(this.descriptor,postSetup,configName);
    }

    @Override
    public Object getResponse() {
        return this.result;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeByteArray(descriptor.toBinary());
        out.writeUTF(postSetup);
        out.writeUTF(configName);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.descriptor = new DeploymentDescriptor();
        this.descriptor.fromBinary(in.readByteArray());
        this.postSetup = in.readUTF();
        this.configName = in.readUTF();
    }
}
