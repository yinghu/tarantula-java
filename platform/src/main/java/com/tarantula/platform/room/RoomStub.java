package com.tarantula.platform.room;

import java.util.Objects;

public class RoomStub {

    public final long roomId;
    public final int sequence;

    public RoomStub(long roomId,int sequence){
        this.roomId = roomId;
        this.sequence = sequence;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof RoomStub)) return false;
        RoomStub stub = (RoomStub)obj;
        return stub.roomId == this.roomId && stub.sequence == this.sequence;
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomId, sequence);
    }
}
