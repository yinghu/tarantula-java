package com.tarantula.platform.service.cluster.tournament;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;

import java.io.IOException;


public class TournamentRegisterOperation extends Operation implements PartitionAwareOperation {

    private String serviceName;
    private String tournamentId;
    private String systemId;
    private String instanceId;

    public TournamentRegisterOperation() {
    }


    public TournamentRegisterOperation(String serviceName, String tournamentId, String systemId) {
        this.serviceName = serviceName;
        this.tournamentId = tournamentId;
        this.systemId = systemId;
    }
    @Override
    public void run() throws Exception {
        TournamentClusterService ais = this.getService();
        instanceId = ais.register(serviceName,tournamentId,systemId);
    }

    @Override
    public Object getResponse() {
        return this.instanceId;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(this.serviceName);
        out.writeUTF(this.tournamentId);
        out.writeUTF(this.systemId);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        serviceName = in.readUTF();
        tournamentId = in.readUTF();
        systemId = in.readUTF();
    }
}
