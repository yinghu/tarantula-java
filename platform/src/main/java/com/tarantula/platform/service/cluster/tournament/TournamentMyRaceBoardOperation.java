package com.tarantula.platform.service.cluster.tournament;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;

import java.io.IOException;


public class TournamentMyRaceBoardOperation extends Operation implements PartitionAwareOperation {

    private String serviceName;

    private long tournamentId;
    private long instanceId;
    private long entryId;
    private long systemId;
    private byte[] raceBoard;

    public TournamentMyRaceBoardOperation() {
    }


    public TournamentMyRaceBoardOperation(String serviceName, long tournamentId, long instanceId,long entryId,long systemId) {
        this.serviceName = serviceName;
        this.tournamentId = tournamentId;
        this.instanceId = instanceId;
    }
    @Override
    public void run() throws Exception {
        TournamentClusterService ais = this.getService();
        raceBoard = ais.myRaceBoard(serviceName,tournamentId,instanceId,entryId,systemId);
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
        out.writeLong(entryId);
        out.writeLong(systemId);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        serviceName = in.readUTF();
        tournamentId = in.readLong();
        instanceId = in.readLong();
        entryId = in.readLong();
        systemId = in.readLong();
    }
}
