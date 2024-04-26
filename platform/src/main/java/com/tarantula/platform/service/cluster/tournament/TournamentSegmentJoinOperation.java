package com.tarantula.platform.service.cluster.tournament;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;

import java.io.IOException;


public class TournamentSegmentJoinOperation extends Operation implements PartitionAwareOperation {

    private String serviceName;
    private long tournamentId;
    private long segmentInstanceId;
    private long systemId;

    private long entered;

    public TournamentSegmentJoinOperation() {
    }


    public TournamentSegmentJoinOperation(String serviceName, long tournamentId, long segmentInstanceId, long systemId) {
        this.serviceName = serviceName;
        this.tournamentId = tournamentId;
        this.segmentInstanceId = segmentInstanceId;
        this.systemId = systemId;
    }
    @Override
    public void run() throws Exception {
        TournamentClusterService ais = this.getService();
        entered = ais.joinOnSegment(serviceName,tournamentId,segmentInstanceId,systemId);
    }

    @Override
    public Object getResponse() {
        return this.entered;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(this.serviceName);
        out.writeLong(tournamentId);
        out.writeLong(segmentInstanceId);
        out.writeLong(this.systemId);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        serviceName = in.readUTF();
        tournamentId = in.readLong();
        segmentInstanceId = in.readLong();
        systemId = in.readLong();
    }
}
