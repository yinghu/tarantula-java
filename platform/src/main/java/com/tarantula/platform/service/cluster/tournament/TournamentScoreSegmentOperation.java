package com.tarantula.platform.service.cluster.tournament;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;

import java.io.IOException;

public class TournamentScoreSegmentOperation extends Operation implements PartitionAwareOperation {

    private String serviceName;
    private long systemId;
    private long tournamentId;
    private long instanceId;
    private long entryId;
    private double credit;
    private double delta;
    private double score;
    public TournamentScoreSegmentOperation() {
    }


    public TournamentScoreSegmentOperation(String serviceName, long tournamentId, long instanceId, long entryId,long systemId, double credit, double delta) {
        this.serviceName = serviceName;
        this.tournamentId = tournamentId;
        this.instanceId = instanceId;
        this.entryId = entryId;
        this.systemId = systemId;
        this.credit = credit;
        this.delta = delta;
    }
    @Override
    public void run() throws Exception {
        TournamentClusterService ais = this.getService();
        this.score = ais.scoreOnSegment(serviceName,tournamentId,instanceId,entryId,systemId,credit,delta);
    }

    @Override
    public Object getResponse() {
        return this.score;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(this.serviceName);
        out.writeLong(this.tournamentId);
        out.writeLong(this.instanceId);
        out.writeLong(this.entryId);
        out.writeLong(this.systemId);
        out.writeDouble(this.credit);
        out.writeDouble(this.delta);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        serviceName = in.readUTF();
        tournamentId = in.readLong();
        instanceId = in.readLong();
        entryId = in.readLong();
        systemId = in.readLong();
        credit = in.readDouble();
        delta = in.readDouble();
    }
}
