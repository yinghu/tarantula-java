package com.tarantula.platform.service.cluster.tournament;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;

import java.io.IOException;


public class EndTournamentOperation extends Operation implements PartitionAwareOperation {

    private String serviceName;
    private String tournamentId;


    public EndTournamentOperation() {
    }


    public EndTournamentOperation(String serviceName, String tournamentId) {
        this.serviceName = serviceName;
        this.tournamentId = tournamentId;
    }
    @Override
    public void run() throws Exception {
        TournamentClusterService ais = this.getService();
        ais.endTournament(serviceName,tournamentId);
    }

    @Override
    public Object getResponse() {
        return null;
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
        serviceName = in.readUTF();
        tournamentId = in.readUTF();
    }
}
