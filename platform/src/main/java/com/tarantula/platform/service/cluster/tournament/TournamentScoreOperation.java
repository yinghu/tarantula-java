package com.tarantula.platform.service.cluster.tournament;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;

import java.io.IOException;

public class TournamentScoreOperation extends Operation implements PartitionAwareOperation {

    private String serviceName;
    private long systemId;
    private long tournamentId;
    private long instanceId;
    private double credit;
    private double delta;
    private double score;
    public TournamentScoreOperation() {
    }


    public TournamentScoreOperation(String serviceName,long tournamentId,long instanceId, long systemId,double credit,double delta) {
        this.serviceName = serviceName;
        this.tournamentId = tournamentId;
        this.instanceId = instanceId;
        this.systemId = systemId;
        this.credit = credit;
        this.delta = delta;
    }
    @Override
    public void run() throws Exception {
        TournamentClusterService ais = this.getService();
        this.score = ais.score(serviceName,tournamentId,instanceId,systemId,credit,delta);
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
        systemId = in.readLong();
        credit = in.readDouble();
        delta = in.readDouble();
    }
}
