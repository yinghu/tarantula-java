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
    }

    public void sync(String[] joined,GameRoom.RoomRegistryListener roomRegistryListener){
        players.clear();
        totalJoined = 0;
        for(String p : joined){
            players.add(p);
            totalJoined++;
        }
        roomRegistryListener.onRegistry(this);
    }
    public String toString(){
        return "Level: "+arenaLevel+" Size: "+maxSize+" Joined: "+totalJoined+" Set: "+players.size();
    }

    @Override
    public boolean equals(Object obj){
        if(obj==this) return true;
        return ((GameRoomRegistry)obj).distributionKey().equals(this.distributionKey());
    }
    @Override
    public int hashCode(){
        return this.distributionKey().hashCode();
    }

    public synchronized int addPlayer(String systemId, GameRoom.RoomRegistryListener roomRegistryListener){
        roomRegistryListener.onRegistry(this);
        //return JOINED;
        return super.addPlayer(systemId);
    }

    public synchronized void removePlayer(String systemId, GameRoom.RoomRegistryListener roomRegistryListener){
        super.removePlayer(systemId);
        roomRegistryListener.onRegistry(this);
    }

    //methods should be called in addPlayer and removePlayer callback
    public boolean empty(){
        return this.totalJoined==0;
    }
    public boolean fullJoined(){
        return totalJoined!=0&&totalJoined==maxSize;
    }
    public void reset(){
        maxSize = 0;
        arenaLevel = 0;
        totalJoined = 0;
        players.clear();
    }

}
