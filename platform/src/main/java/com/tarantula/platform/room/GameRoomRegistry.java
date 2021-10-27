package com.tarantula.platform.room;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.game.Arena;
import com.tarantula.platform.RoomRegistry;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;
import java.util.Map;

public class GameRoomRegistry extends RoomRegistry implements Portable {

    public int arenaLevel;

    public GameRoomRegistry(){
        super();
    }
    public GameRoomRegistry(Arena arena){
        super(arena.capacity);
        this.arenaLevel = arena.level;
    }

    @Override
    public Map<String,Object> toMap(){
        super.toMap();
        properties.put("level",arenaLevel);
        properties.put("size",maxSize);
        return properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        arenaLevel = ((Number)properties.remove("level")).intValue();
        maxSize = ((Number)properties.remove("size")).intValue();
        super.fromMap(properties);
    }

    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }
    public int getClassId() {
        return PortableEventRegistry.GAME_ROOM_REGISTRY_CID;
    }

    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeUTF("1",this.bucket);
        portableWriter.writeUTF("2",this.oid);
        portableWriter.writeInt("3",arenaLevel);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        this.bucket = portableReader.readUTF("1");
        this.oid = portableReader.readUTF("2");
        this.arenaLevel = portableReader.readInt("3");
    }
}
