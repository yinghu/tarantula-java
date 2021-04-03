package com.tarantula.platform.service.cluster.deployment;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.icodesoftware.Descriptor;
import com.tarantula.platform.LobbyDescriptor;

import java.io.IOException;

public class AddLobbyOperation extends Operation {


    private Descriptor descriptor;
    private String publishingId;
    private boolean result;

    public AddLobbyOperation() {
    }


    public AddLobbyOperation(Descriptor lobby,String publishingId) {
        this.descriptor = lobby;
        this.publishingId = publishingId;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        this.result = cds.addLobby(this.descriptor,publishingId);
    }

    @Override
    public Object getResponse() {
        return this.result;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(publishingId);
        out.writeByteArray(this.descriptor.toBinary());
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        publishingId = in.readUTF();
        this.descriptor = new LobbyDescriptor();
        this.descriptor.fromBinary(in.readByteArray());
    }
}
