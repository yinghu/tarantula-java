package com.tarantula.platform.service.cluster.tournament;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;

import java.io.IOException;


public class RegisterTournamentOperation extends Operation implements PartitionAwareOperation {

    private String serviceName;
    private long tournamentId;

    private int slot;

    private long entered;

    public RegisterTournamentOperation() {
    }


    public RegisterTournamentOperation(String serviceName, long tournamentId, int slot) {
        this.serviceName = serviceName;
        this.tournamentId = tournamentId;
        this.slot = slot;
    }
    @Override
    public void run() throws Exception {
        TournamentClusterService ais = this.getService();
        entered = ais.register(serviceName,tournamentId,slot);
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
        out.writeInt(slot);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        serviceName = in.readUTF();
        tournamentId = in.readLong();
        slot = in.readInt();
    }
}
