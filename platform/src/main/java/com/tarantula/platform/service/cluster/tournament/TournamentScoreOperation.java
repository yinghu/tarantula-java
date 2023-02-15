package com.tarantula.platform.service.cluster.tournament;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;
import com.icodesoftware.Tournament;

import java.io.IOException;

public class TournamentScoreOperation extends Operation implements PartitionAwareOperation {

    private String serviceName;
    private String systemId;
    private String tournamentId;
    private String instanceId;
    private double delta;
    private Tournament.Entry entry;
    public TournamentScoreOperation() {
    }


    public TournamentScoreOperation(String serviceName,String tournamentId,String instanceId, String systemId,double delta) {
        this.serviceName = serviceName;
        this.tournamentId = tournamentId;
        this.instanceId = instanceId;
        this.systemId = systemId;
        this.delta = delta;
    }
    @Override
    public void run() throws Exception {
        TournamentClusterService ais = this.getService();
        this.entry = ais.score(serviceName,tournamentId,instanceId,systemId,delta);
    }

    @Override
    public Object getResponse() {
        return this.entry;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(this.serviceName);
        out.writeUTF(this.tournamentId);
        out.writeUTF(this.instanceId);
        out.writeUTF(this.systemId);
        out.writeDouble(this.delta);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        serviceName = in.readUTF();
        tournamentId = in.readUTF();
        instanceId = in.readUTF();
        systemId = in.readUTF();
        delta = in.readDouble();
    }
}
