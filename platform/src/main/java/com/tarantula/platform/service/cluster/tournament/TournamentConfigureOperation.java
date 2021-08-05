package com.tarantula.platform.service.cluster.tournament;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;
import com.icodesoftware.Tournament;

import java.io.IOException;

public class TournamentConfigureOperation extends Operation implements PartitionAwareOperation {

    private String serviceName;
    private String systemId;
    private String instanceId;
    private byte[] payload;
    private Tournament.Entry entry;
    public TournamentConfigureOperation() {
    }
    public TournamentConfigureOperation(String serviceName, String instanceId, String systemId,byte[] payload) {
        this.serviceName = serviceName;
        this.instanceId = instanceId;
        this.systemId = systemId;
        this.payload = payload;
    }
    @Override
    public void run() throws Exception {
        TournamentClusterService ais = this.getService();
        this.entry = ais.configure(serviceName,instanceId,systemId,payload);
    }

    @Override
    public Object getResponse() {
        return this.entry;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(this.serviceName);
        out.writeUTF(this.instanceId);
        out.writeUTF(this.systemId);
        out.writeByteArray(payload);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        serviceName = in.readUTF();
        instanceId = in.readUTF();
        systemId = in.readUTF();
        payload = in.readByteArray();
    }
}
