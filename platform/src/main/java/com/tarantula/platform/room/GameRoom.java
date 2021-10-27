package com.tarantula.platform.room;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Module;
import com.icodesoftware.Tournament;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.game.Arena;
import com.tarantula.game.GameEntry;
import com.tarantula.game.service.GameEntryQuery;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;
import java.util.Map;

public class GameRoom extends RecoverableObject implements Portable {

    private boolean offline;
    private int capacity;
    private boolean tournamentEnabled;
    private long duration;
    private int round;
    private Arena arena;
    private Tournament.Instance instance;

    private GameEntry[] playList;

    public GameRoom(){
    }
    public GameRoom(Arena arena){
        this();
        this.offline = arena.capacity==1;
        playList = new GameEntry[capacity];
    }
    public int round(){
        return round;
    }
    public boolean offline(){
        return offline;
    }
    public int capacity(){
        return capacity;
    }
    public boolean tournamentEnabled(){
        return tournamentEnabled;
    }
    public Arena arena(){
        return this.arena;
    }
    public Tournament.Instance tournament(){
        return this.instance;
    }


    public String roomId(){
        return this.distributionKey();
    }
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",offline);
        this.properties.put("2",capacity);
        this.properties.put("3",tournamentEnabled);
        this.properties.put("4",duration);
        this.properties.put("5",round);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.offline = (boolean)properties.getOrDefault("1",true);
        this.capacity = ((Number)properties.getOrDefault("2",0)).intValue();
        this.tournamentEnabled = (boolean)properties.getOrDefault("3",false);
        this.duration = ((Number)properties.getOrDefault("4",0)).longValue();
        this.round = ((Number)properties.getOrDefault("5",0)).intValue();
    }
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PortableEventRegistry.ROOM_CID;
    }

    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeUTF("1",this.distributionKey());
        portableWriter.writeBoolean("2",offline);
        portableWriter.writeInt("3",capacity);
        portableWriter.writeLong("4",duration);
        portableWriter.writeInt("5",round);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        this.distributionKey(portableReader.readUTF("1"));
        this.offline = portableReader.readBoolean("2");
        this.capacity = portableReader.readInt("3");
        this.duration = portableReader.readLong("4");
        this.round = portableReader.readInt("5");
    }
    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("roomId",distributionKey());
        jsonObject.addProperty("offline",offline);
        jsonObject.addProperty("totalJoined",capacity);
        jsonObject.addProperty("tournamentEnabled",tournamentEnabled);
        jsonObject.addProperty("duration",duration);
        jsonObject.addProperty("round",round);
        JsonArray plist = new JsonArray();
        jsonObject.add("list",plist);
        return jsonObject;
    }
    public void onTimer(Module.OnUpdate onUpdate){
        
    }
    public void setup(Arena arena,Tournament.Instance instance){
        this.arena = arena;
        this.instance = instance;
        this.tournamentEnabled = this.instance!=null;
        this.duration = arena.duration;
        this.offline = arena.capacity==1;
        this.round++;
    }
    public void reset(){

    }
    public void load(){
        dataStore.list(new GameEntryQuery(this.distributionKey()),(ge)->{
            playList[ge.seat]=ge;
            return true;
        });
    }
    public void join(String systemId){

    }
}