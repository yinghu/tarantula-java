package com.tarantula.platform.service.cluster.tournament;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;
import com.icodesoftware.Tournament;

import java.io.IOException;


public class EnterTournamentOperation extends Operation implements PartitionAwareOperation {

    private String serviceName;
    private long tournamentId;

    private long systemId;

    private boolean entered;

    public EnterTournamentOperation() {
    }


    public EnterTournamentOperation(String serviceName, long tournamentId,long systemId) {
        this.serviceName = serviceName;
        this.tournamentId = tournamentId;
        this.systemId = systemId;
    }
    @Override
    public void run() throws Exception {
        TournamentClusterService ais = this.getService();
        entered = ais.enter(serviceName,tournamentId,systemId);
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
        out.writeLong(this.systemId);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        serviceName = in.readUTF();
        tournamentId = in.readLong();
        systemId = in.readLong();
    }
}
