package com.tarantula.platform.service.cluster.room;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;
import com.tarantula.game.Arena;
import com.tarantula.game.GameRoom;
import com.tarantula.game.Rating;
import com.tarantula.platform.room.GameRoomRegistry;

import java.io.IOException;

public class RoomRegisterOperation extends Operation implements PartitionAwareOperation {

    private String serviceName;
    private String zoneId;
    private Rating rating;
    private GameRoomRegistry gameRoomRegistry;
    public RoomRegisterOperation(){}
    public RoomRegisterOperation(String serviceName, String zoneId, Rating rating){
        this.serviceName = serviceName;
        this.zoneId = zoneId;
        this.rating = rating;
    }

    @Override
    public void run() throws Exception {
        RoomClusterService ais = this.getService();
        gameRoomRegistry = ais.register(serviceName,zoneId,rating);
    }

    @Override
    public Object getResponse() {
        return gameRoomRegistry;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(serviceName);
        out.writeUTF(zoneId);
        out.writeObject(rating);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        serviceName = in.readUTF();
        zoneId = in.readUTF();
        rating = in.readObject();
    }
}
