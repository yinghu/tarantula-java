package com.tarantula.platform.service.cluster.tournament;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;


import java.io.IOException;


public class SyncTournamentOperation extends Operation implements PartitionAwareOperation {

    private String serviceName;
    private String tournamentId;
    private String instanceId;

    public SyncTournamentOperation() {
    }


    public SyncTournamentOperation(String serviceName, String tournamentId, String instanceId) {
        this.serviceName = serviceName;
        this.tournamentId = tournamentId;
        this.instanceId = instanceId;
    }
    @Override
    public void run() throws Exception {
        TournamentClusterService ais = this.getService();
        ais.syncTournament(serviceName,tournamentId,instanceId);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(this.serviceName);
        out.writeUTF(tournamentId);
        out.writeUTF(this.instanceId);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        serviceName = in.readUTF();
        tournamentId = in.readUTF();
        instanceId = in.readUTF();
    }
}
