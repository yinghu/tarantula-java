package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.tarantula.Descriptor;
import com.tarantula.platform.LobbyDescriptor;
import com.tarantula.platform.util.SystemUtil;

import java.io.IOException;

/**
 * created by yinghu lu on 5/15/2020.
 */
public class CreateGameClusterOperation extends Operation {


    private Descriptor descriptor;

    private String result;

    public CreateGameClusterOperation() {
    }


    public CreateGameClusterOperation(Descriptor lobby) {
        this.descriptor = lobby;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        this.result = cds.createGameCluster(this.descriptor);
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
