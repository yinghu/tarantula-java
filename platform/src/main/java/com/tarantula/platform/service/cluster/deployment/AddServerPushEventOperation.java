package com.tarantula.platform.service.cluster.deployment;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.icodesoftware.Event;
import com.tarantula.platform.event.ServerPushEvent;

import java.io.IOException;

/**
 * Created by yinghu lu on 7/25/2020
 */
public class AddServerPushEventOperation extends Operation {

    private Event event;
    public AddServerPushEventOperation() {
    }
    public AddServerPushEventOperation(Event event) {
        this.event = event;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        cds.addServerPushEvent(event);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(event.source());
        out.writeUTF(event.sessionId());
        out.writeUTF(event.trackId());
        out.writeUTF(event.clientId());
        out.writeUTF(event.typeId());
        out.writeByteArray(event.payload());
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        event = new ServerPushEvent(in.readUTF(),in.readUTF(),in.readUTF(),in.readUTF(),in.readUTF(),in.readByteArray());
    }
}
