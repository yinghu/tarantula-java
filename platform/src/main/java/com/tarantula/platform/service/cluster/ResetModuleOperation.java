package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.tarantula.Descriptor;
import com.tarantula.platform.DeploymentDescriptor;

import java.io.IOException;

/**
 * updated by yinghu lu on 5/29/2019.
 */
public class ResetModuleOperation extends Operation {

    private String lobbyId;
    private String  subtypeId;
    private String codebase;
    private String artifact;
    private String version;
    private boolean result;

    public ResetModuleOperation() {
    }


    public ResetModuleOperation(String lobbyId,Descriptor descriptor) {
        this.lobbyId = lobbyId;
        this.subtypeId = descriptor.subtypeId();
        this.codebase = descriptor.codebase();
        this.artifact = descriptor.moduleArtifact();
        this.version = descriptor.moduleVersion();
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        Descriptor descriptor = new DeploymentDescriptor();
        descriptor.subtypeId(subtypeId);
        descriptor.codebase(codebase);
        descriptor.moduleArtifact(artifact);
        descriptor.moduleVersion(version);
        this.result = cds.resetModule(lobbyId,descriptor);
    }

    @Override
    public Object getResponse() {
        return this.result;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(lobbyId);
        out.writeUTF(subtypeId);
        out.writeUTF(codebase);
        out.writeUTF(artifact);
        out.writeUTF(version);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.lobbyId = in.readUTF();
        this.subtypeId = in.readUTF();
        this.codebase = in.readLine();
        this.artifact = in.readUTF();
        this.version = in.readUTF();
    }
}
