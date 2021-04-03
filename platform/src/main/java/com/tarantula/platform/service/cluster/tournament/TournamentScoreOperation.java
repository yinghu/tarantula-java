package com.tarantula.platform.service.cluster.tournament;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;

import java.io.IOException;

public class TournamentScoreOperation extends Operation implements PartitionAwareOperation {

    private String serviceName;
    private String systemId;
    private String instanceId;
    private double delta;
    private byte[] data;
    public TournamentScoreOperation() {
    }


    public TournamentScoreOperation(String serviceName, String instanceId, String systemId,double delta) {
        this.serviceName = serviceName;
        this.instanceId = instanceId;
        this.systemId = systemId;
        this.delta = delta;
    }
    @Override
    public void run() throws Exception {
        TournamentClusterService ais = this.getService();
        this.data = ais.score(serviceName,instanceId,systemId,delta).toBinary();
    }

    @Override
    public Object getResponse() {
        return this.data;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(this.serviceName);
        out.writeUTF(this.instanceId);
        out.writeUTF(this.systemId);
        out.writeDouble(this.delta);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        serviceName = in.readUTF();
        instanceId = in.readUTF();
        systemId = in.readUTF();
        delta = in.readDouble();
    }
}
