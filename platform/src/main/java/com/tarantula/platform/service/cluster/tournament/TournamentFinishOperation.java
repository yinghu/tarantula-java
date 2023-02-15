package com.tarantula.platform.service.cluster.tournament;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;


import java.io.IOException;

public class TournamentFinishOperation extends Operation implements PartitionAwareOperation {

    private String serviceName;
    private String systemId;
    private String tournamentId;
    private String instanceId;

    public TournamentFinishOperation() {
    }


    public TournamentFinishOperation(String serviceName, String tournamentId,String instanceId, String systemId) {
        this.serviceName = serviceName;
        this.tournamentId = tournamentId;
        this.instanceId = instanceId;
        this.systemId = systemId;
    }
    @Override
    public void run() throws Exception {
        TournamentClusterService ais = this.getService();
        ais.finish(serviceName,tournamentId,instanceId,systemId);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(this.serviceName);
        out.writeUTF(this.tournamentId);
        out.writeUTF(this.instanceId);
        out.writeUTF(this.systemId);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.serviceName = in.readUTF();
        this.tournamentId = in.readUTF();
        this.instanceId = in.readUTF();
        this.systemId = in.readUTF();
    }
}
