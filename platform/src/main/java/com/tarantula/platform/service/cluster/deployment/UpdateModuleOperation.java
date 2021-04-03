package com.tarantula.platform.service.cluster.deployment;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.icodesoftware.Descriptor;
import com.tarantula.platform.DeploymentDescriptor;

import java.io.IOException;

public class UpdateModuleOperation extends Operation {

    private String typeId;
    private String codebase;
    private String artifact;
    private String version;

    public UpdateModuleOperation() {
    }


    public UpdateModuleOperation(Descriptor descriptor) {
        this.typeId = descriptor.typeId();
        this.codebase = descriptor.codebase();
        this.artifact = descriptor.moduleArtifact();
        this.version = descriptor.moduleVersion();
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        Descriptor descriptor = new DeploymentDescriptor();
        descriptor.typeId(typeId);
        descriptor.codebase(codebase);
        descriptor.moduleArtifact(artifact);
        descriptor.moduleVersion(version);
        cds.updateModule(descriptor);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(typeId);
        out.writeUTF(codebase);
        out.writeUTF(artifact);
        out.writeUTF(version);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.typeId = in.readUTF();
        this.codebase = in.readUTF();
        this.artifact = in.readUTF();
        this.version = in.readUTF();
    }
}
