package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.tarantula.Descriptor;
import com.tarantula.platform.LobbyDescriptor;
import com.tarantula.platform.util.SystemUtil;

import java.io.IOException;

/**
 * updated by yinghu lu on 5/29/2019.
 */
public class AddLobbyOperation extends Operation {


    private Descriptor descriptor;

    private boolean result;

    public AddLobbyOperation() {
    }


    public AddLobbyOperation(Descriptor lobby) {
        this.descriptor = lobby;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        this.result = cds.addLobby(this.descriptor);
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
        this.descriptor = new LobbyDescriptor();
        this.descriptor.fromMap(SystemUtil.toMap(in.readByteArray()));
    }
}
