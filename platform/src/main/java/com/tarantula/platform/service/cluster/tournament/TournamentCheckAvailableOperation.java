package com.tarantula.platform.service.cluster.tournament;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;

import java.io.IOException;

public class TournamentCheckAvailableOperation extends Operation implements PartitionAwareOperation {

    private String serviceName;
    private String tournamentId;
    private boolean available;

    public TournamentCheckAvailableOperation() {
    }


    public TournamentCheckAvailableOperation(String serviceName, String tournamentId) {
        this.serviceName = serviceName;
        this.tournamentId = tournamentId;
    }
    @Override
    public void run() throws Exception {
        TournamentClusterService ais = this.getService();
        available = ais.checkAvailable(serviceName,tournamentId);
    }

    @Override
    public Object getResponse() {
        return this.available;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(this.serviceName);
        out.writeUTF(this.tournamentId);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
    }
}
