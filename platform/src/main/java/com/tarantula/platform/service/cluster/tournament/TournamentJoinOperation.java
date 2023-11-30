package com.tarantula.platform.service.cluster.tournament;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;
import com.icodesoftware.Tournament;

import java.io.IOException;


public class TournamentJoinOperation extends Operation implements PartitionAwareOperation {

    private String serviceName;
    private long tournamentId;
    private long instanceId;
    private long systemId;
    private Tournament.Instance instance;

    public TournamentJoinOperation() {
    }


    public TournamentJoinOperation(String serviceName, long tournamentId, long instanceId, long systemId) {
        this.serviceName = serviceName;
        this.tournamentId = tournamentId;
        this.instanceId = instanceId;
        this.systemId = systemId;
    }
    @Override
    public void run() throws Exception {
        TournamentClusterService ais = this.getService();
        instance = ais.join(serviceName,tournamentId,instanceId,systemId);
    }

    @Override
    public Object getResponse() {
        return this.instance;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(this.serviceName);
        out.writeLong(tournamentId);
        out.writeLong(this.instanceId);
        out.writeLong(this.systemId);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        serviceName = in.readUTF();
        tournamentId = in.readLong();
        instanceId = in.readLong();
        systemId = in.readLong();
    }
}
