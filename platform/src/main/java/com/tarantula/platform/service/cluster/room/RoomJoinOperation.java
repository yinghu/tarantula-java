package com.tarantula.platform.service.cluster.room;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;
import com.tarantula.game.Arena;
import com.tarantula.game.GameRoom;

import java.io.IOException;

public class RoomJoinOperation extends Operation implements PartitionAwareOperation {

    private String serviceName;
    private Arena arena;
    private String roomId;
    private String systemId;

    private GameRoom stub;
    public RoomJoinOperation(){}
    public RoomJoinOperation(String serviceName, Arena arena, String roomId, String systemId){
        this.serviceName = serviceName;
        this.arena = arena;
        this.roomId = roomId;
        this.systemId = systemId;
    }

    @Override
    public void run() throws Exception {
        RoomClusterService ais = this.getService();
        stub = ais.join(serviceName,arena,roomId,systemId);
    }

    @Override
    public Object getResponse() {
        return stub;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(serviceName);
        out.writeObject(arena);
        out.writeUTF(roomId);
        out.writeUTF(systemId);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        serviceName = in.readUTF();
        arena = in.readObject();
        roomId = in.readUTF();
        systemId = in.readUTF();
    }
}
