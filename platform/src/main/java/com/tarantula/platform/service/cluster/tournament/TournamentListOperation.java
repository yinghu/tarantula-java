package com.tarantula.platform.service.cluster.tournament;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;
import com.icodesoftware.Tournament;

import java.io.IOException;


public class TournamentListOperation extends Operation implements PartitionAwareOperation {

    private String serviceName;

    private String instanceId;

    private Tournament.RaceBoard raceBoard;

    public TournamentListOperation() {
    }


    public TournamentListOperation(String serviceName,  String instanceId) {
        this.serviceName = serviceName;
        this.instanceId = instanceId;
    }
    @Override
    public void run() throws Exception {
        TournamentClusterService ais = this.getService();
        raceBoard = ais.list(serviceName,instanceId);
    }

    @Override
    public Object getResponse() {
        return this.raceBoard;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(this.serviceName);
        out.writeUTF(this.instanceId);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        serviceName = in.readUTF();
        instanceId = in.readUTF();
    }
}
