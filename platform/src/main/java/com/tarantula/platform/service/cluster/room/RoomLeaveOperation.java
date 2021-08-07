package com.tarantula.platform.service.cluster.room;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;

import java.io.IOException;

public class RoomLeaveOperation extends Operation implements PartitionAwareOperation {

    private String serviceName;
    private String roomId;
    private String systemId;
    public RoomLeaveOperation(){}
    public RoomLeaveOperation(String serviceName, String roomId, String systemId){
        this.serviceName = serviceName;
        this.roomId = roomId;
        this.systemId = systemId;
    }

    @Override
    public void run() throws Exception {
        RoomClusterService ais = this.getService();
        ais.leave(serviceName,roomId,systemId);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(serviceName);
        out.writeUTF(roomId);
        out.writeUTF(systemId);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        serviceName = in.readUTF();
        roomId = in.readUTF();
        systemId = in.readUTF();
    }
}
