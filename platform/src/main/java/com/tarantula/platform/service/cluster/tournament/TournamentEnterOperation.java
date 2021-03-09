package com.tarantula.platform.service.cluster.tournament;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;
import com.icodesoftware.Tournament;
import com.icodesoftware.util.JsonUtil;

import java.io.IOException;
import java.util.Map;

/**
 * created by yinghu lu on 3/5/2021.
 */
public class TournamentEnterOperation extends Operation implements PartitionAwareOperation {

    private String serviceName;
    private String tournamentId;
    private String instanceId;
    private String systemId;
    private byte[] data;

    public TournamentEnterOperation() {
    }


    public TournamentEnterOperation(String serviceName, String tournamentId,String instanceId, String systemId) {
        this.serviceName = serviceName;
        this.tournamentId = tournamentId;
        this.instanceId = instanceId;
        this.systemId = systemId;
    }
    @Override
    public void run() throws Exception {
        TournamentClusterService ais = this.getService();
        Tournament.Instance entry = ais.enter(serviceName,tournamentId,instanceId,systemId);
        Map<String,Object> _map = entry.toMap();
        this.data = JsonUtil.toJson(_map);
    }

    @Override
    public Object getResponse() {
        return this.data;
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
