package com.tarantula.platform.service.cluster.tournament;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;


import java.io.IOException;


public class EndTournamentOperation extends Operation {

    private String serviceName;
    private long tournamentId;


    public EndTournamentOperation() {
    }


    public EndTournamentOperation(String serviceName, long tournamentId) {
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
        out.writeLong(this.tournamentId);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        serviceName = in.readUTF();
        tournamentId = in.readLong();
    }
}
