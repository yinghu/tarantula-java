package com.tarantula.platform.service.cluster.tournament;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;


import java.io.IOException;


public class TournamentRaceBoardOperation extends Operation implements PartitionAwareOperation {

    private String serviceName;

    private long tournamentId;
    private long instanceId;

    private byte[] raceBoard;

    public TournamentRaceBoardOperation() {
    }


    public TournamentRaceBoardOperation(String serviceName, long tournamentId, long instanceId) {
        this.serviceName = serviceName;
        this.tournamentId = tournamentId;
        this.instanceId = instanceId;
    }
    @Override
    public void run() throws Exception {
        TournamentClusterService ais = this.getService();
        raceBoard = ais.raceBoard(serviceName,tournamentId,instanceId);
    }

    @Override
    public Object getResponse() {
        return this.raceBoard;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(this.serviceName);
        out.writeLong(this.tournamentId);
        out.writeLong(this.instanceId);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        serviceName = in.readUTF();
        tournamentId = in.readLong();
        instanceId = in.readLong();
    }
}
