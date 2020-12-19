package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;
import com.icodesoftware.Session;
import com.tarantula.platform.event.FastPlayEvent;
import com.tarantula.platform.event.SessionForward;

import java.io.IOException;

/**
 * created by yinghu lu on 12/16/2020.
 */
public class GetConnectionOperation extends Operation implements PartitionAwareOperation {

    private String lobbyTag;
    private Session session;

    public GetConnectionOperation() {
    }

    public GetConnectionOperation(String lobbyTag, Session session) {
        this.lobbyTag = lobbyTag;
        this.session = session;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService ais = this.getService();
        ais.getConnection(lobbyTag,session);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(lobbyTag);
        out.writeUTF(session.systemId());
        out.writeInt(session.stub());
        out.writeUTF(session.source());
        out.writeUTF(session.sessionId());
        out.writeByteArray(session.payload());
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.lobbyTag = in.readUTF();
        session = new FastPlayEvent(in.readUTF(),in.readInt(),new SessionForward(in.readUTF(),in.readUTF()));
        session.payload(in.readByteArray());
    }

}
