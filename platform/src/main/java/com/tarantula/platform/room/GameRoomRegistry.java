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

    public static final String LABEL = "GRY";
    public int arenaLevel;
    public String joinTicket;
    public GameRoomRegistry(){
        super();
        this.label = LABEL;
        this.onEdge = true;
    }
    public GameRoomRegistry(Arena arena){
        super(arena.capacity);
        this.label = LABEL;
        this.onEdge = true;
        this.arenaLevel = arena.level;
    }

    @Override
    public Map<String,Object> toMap(){
        super.toMap();
        properties.put("level",arenaLevel);
        properties.put("size",maxSize);
        properties.put("ticket",joinTicket);
        return properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        arenaLevel = ((Number)properties.remove("level")).intValue();
        maxSize = ((Number)properties.remove("size")).intValue();
        joinTicket = (String)properties.remove("ticket");
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
        portableWriter.writeUTF("4",joinTicket);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        this.bucket = portableReader.readUTF("1");
        this.oid = portableReader.readUTF("2");
        this.arenaLevel = portableReader.readInt("3");
        this.joinTicket = portableReader.readUTF("4");
    }
    public void reset(Arena arena){
        maxSize = arena.capacity;
        arenaLevel = arena.level;
        players.clear();
    }
    public void reset(){
        maxSize = 0;
        arenaLevel = 0;
        players.clear();
    }
    public String toString(){
        return "Level: "+arenaLevel+" Size: "+maxSize+" Joined: "+totalJoined;
    }
}
