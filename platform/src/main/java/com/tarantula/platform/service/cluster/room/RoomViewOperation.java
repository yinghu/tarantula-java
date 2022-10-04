package com.tarantula.platform.service.cluster.room;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;
import com.tarantula.platform.room.GameRoom;

import java.io.IOException;

public class RoomViewOperation extends Operation implements PartitionAwareOperation {

    private String serviceName;
    private String zoneId;
    private String roomId;


    private GameRoom stub;
    public RoomViewOperation(){}
    public RoomViewOperation(String serviceName,String zoneId, String roomId){
        this.serviceName = serviceName;
        this.zoneId = zoneId;
        this.roomId = roomId;
    }

    @Override
    public void run() throws Exception {
        RoomClusterService ais = this.getService();
        stub = ais.view(serviceName,zoneId,roomId);
    }

    @Override
    public Object getResponse() {
        return stub;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(serviceName);
        out.writeUTF(zoneId);
        out.writeUTF(roomId);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        serviceName = in.readUTF();
        zoneId = in.readUTF();
        roomId = in.readUTF();
    }
}
