package com.tarantula.platform.service.cluster.room;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;
import com.tarantula.game.Arena;
import com.tarantula.game.GameRoom;
import com.tarantula.game.Rating;

import java.io.IOException;

public class RoomRegisterOperation extends Operation implements PartitionAwareOperation {

    private String serviceName;
    private Arena arena;
    private Rating rating;
    private String roomId;
    public RoomRegisterOperation(){}
    public RoomRegisterOperation(String serviceName, Arena arena, Rating rating){
        this.serviceName = serviceName;
        this.arena = arena;
        this.rating = rating;
    }

    @Override
    public void run() throws Exception {
        RoomClusterService ais = this.getService();
        roomId = ais.register(serviceName,arena,rating);
    }

    @Override
    public Object getResponse() {
        return roomId;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(serviceName);
        out.writeObject(arena);
        out.writeObject(rating);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        serviceName = in.readUTF();
        arena = in.readObject();
        rating = in.readObject();
    }
}
