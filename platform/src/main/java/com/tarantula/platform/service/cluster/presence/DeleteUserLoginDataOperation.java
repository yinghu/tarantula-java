package com.tarantula.platform.service.cluster.presence;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;

import java.io.IOException;

public class DeleteUserLoginDataOperation extends Operation implements PartitionAwareOperation {
    private long playerID;

    private boolean successful;

    public DeleteUserLoginDataOperation(){

    }

    public DeleteUserLoginDataOperation(long playerID) {
        this.playerID = playerID;
    }

    @Override
    public void run() throws Exception {
        PresenceClusterService ais = this.getService();
        successful = ais.delete(playerID);
    }

    @Override
    public Object getResponse() {
        return this.successful;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeLong(this.playerID);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.playerID = in.readLong();
    }
}
