package com.tarantula.platform.service.cluster.tournament;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;
import com.icodesoftware.Tournament;

import java.io.IOException;


public class TournamentJoinOperation extends Operation implements PartitionAwareOperation {

    private String serviceName;
    private String tournamentId;
    private String instanceId;
    private String systemId;
    private Tournament.Instance instance;

    public TournamentJoinOperation() {
    }


    public TournamentJoinOperation(String serviceName, String tournamentId, String instanceId, String systemId) {
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
        out.writeUTF(tournamentId);
        out.writeUTF(this.instanceId);
        out.writeUTF(this.systemId);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        serviceName = in.readUTF();
        tournamentId = in.readUTF();
        instanceId = in.readUTF();
        systemId = in.readUTF();
    }
}
