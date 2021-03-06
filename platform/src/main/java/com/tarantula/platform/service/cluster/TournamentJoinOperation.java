package com.tarantula.platform.service.cluster;

import com.hazelcast.internal.json.Json;
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
public class TournamentJoinOperation extends Operation implements PartitionAwareOperation {

    private String serviceName;
    private String tournamentId;
    private String systemId;
    private byte[] data;

    public TournamentJoinOperation() {
    }


    public TournamentJoinOperation(String serviceName,String tournamentId,String systemId) {
        this.serviceName = serviceName;
        this.tournamentId = tournamentId;
        this.systemId = systemId;
    }
    @Override
    public void run() throws Exception {
        TournamentClusterService ais = this.getService();
        Tournament.Entry entry = ais.join(serviceName,tournamentId,systemId);
        Map<String,Object> _map = entry.toMap();
        _map.put("instanceId",entry.owner());
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
        out.writeUTF(this.tournamentId);
        out.writeUTF(this.systemId);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
    }
}
