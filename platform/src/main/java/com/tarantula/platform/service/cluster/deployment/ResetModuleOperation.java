package com.tarantula.platform.service.cluster.deployment;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.icodesoftware.Descriptor;
import com.tarantula.platform.DeploymentDescriptor;

import java.io.IOException;

/**
 * updated by yinghu lu on 5/29/2019.
 */
public class ResetModuleOperation extends Operation {

    private String typeId;
    private String codebase;
    private String artifact;
    private String version;
    private String index;
    private boolean result;

    public ResetModuleOperation() {
    }


    public ResetModuleOperation(Descriptor descriptor) {
        this.typeId = descriptor.typeId();
        this.codebase = descriptor.codebase();
        this.artifact = descriptor.moduleArtifact();
        this.version = descriptor.moduleVersion();
        this.index = descriptor.index();
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        Descriptor descriptor = new DeploymentDescriptor();
        descriptor.typeId(typeId);
        descriptor.codebase(codebase);
        descriptor.moduleArtifact(artifact);
        descriptor.moduleVersion(version);
        descriptor.index(index);
        this.result = cds.resetModule(descriptor);
    }

    @Override
    public Object getResponse() {
        return this.result;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(typeId);
        out.writeUTF(codebase);
        out.writeUTF(artifact);
        out.writeUTF(version);
        out.writeUTF(index);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.typeId = in.readUTF();
        this.codebase = in.readLine();
        this.artifact = in.readUTF();
        this.version = in.readUTF();
        this.index = in.readUTF();
    }
}
