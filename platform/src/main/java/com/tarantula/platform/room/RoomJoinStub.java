package com.tarantula.platform.room;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;

public class RoomJoinStub implements Portable {
    public int level;
    public String roomId;
    public boolean joined;

    public RoomJoinStub(){}
    public RoomJoinStub(int level,String roomId){
        this.level = level;
        this.roomId = roomId;
        this.joined = true;
    }
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableEventRegistry.ROOM_JOIN_STUB;
    }

    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeUTF("1",roomId);
        portableWriter.writeInt("3",level);
        portableWriter.writeBoolean("4",joined);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        roomId = portableReader.readUTF("1");
        level = portableReader.readInt("3");
        joined = portableReader.readBoolean("4");
    }
    @Override
    public String toString() {
        return "Room["+roomId+"]["+level+"]["+joined+"]";
    }
}
