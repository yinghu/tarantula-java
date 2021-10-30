package com.tarantula.platform.room;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.game.Arena;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

abstract public class GameRoomHeader extends RecoverableObject implements GameRoom {

    protected int capacity;
    protected long duration;
    protected int round;
    protected Arena arena;

    protected HashMap<String,GameEntry> joinIndex;
    protected GameEntry[] entries;


    @Override
    public String roomId(){
        return this.distributionKey();
    }
    @Override
    public long duration() {
        return duration;
    }

    @Override
    public int capacity() {
        return capacity;
    }
    @Override
    public int round() {
        return round;
    }

    @Override
    public Arena arena() {
        return arena;
    }

    public void setup(Arena arena){
        this.arena = arena;
        this.capacity = arena.capacity;
        this.duration = arena.duration;
    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("2",capacity);
        this.properties.put("3",round);
        this.properties.put("4",this.index);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.capacity = ((Number)properties.getOrDefault("2",12)).intValue();
        this.round = ((Number)properties.getOrDefault("3",0)).intValue();
        this.index = (String)properties.getOrDefault("4",null);
    }
    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("capacity",capacity);
        jsonObject.addProperty("duration",duration);
        jsonObject.addProperty("round",round);
        JsonArray plist = new JsonArray();
        for(GameEntry ge : entries){
            if(ge==null) continue;
            plist.add(ge.toJson());
        }
        jsonObject.add("onList",plist);
        return jsonObject;
    }

    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeUTF("1",this.distributionKey());
        portableWriter.writeInt("3",round);
        portableWriter.writeInt("4",capacity);
        portableWriter.writePortableArray("5",entries);
    }

    public void readPortable(PortableReader portableReader) throws IOException {
        this.distributionKey(portableReader.readUTF("1"));
        this.round = portableReader.readInt("3");
        entries = new GameEntry[portableReader.readInt("4")];
        for(Portable p : portableReader.readPortableArray("5")){
            GameEntry gameEntry = (GameEntry)p;
            entries[gameEntry.seatIndex]=gameEntry;
        }
    }
}
