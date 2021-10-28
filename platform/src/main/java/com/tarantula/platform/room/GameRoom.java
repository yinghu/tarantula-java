package com.tarantula.platform.room;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Module;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.game.Arena;
import com.tarantula.game.service.GameEntryQuery;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GameRoom extends RecoverableObject implements Portable {

    private int capacity;
    private long duration;
    private int round;
    private Arena arena;

    private HashMap<String,GameEntry> joinIndex;
    private GameEntry[] entries;

    public GameRoom(int capacity){
        this.capacity = capacity;
        joinIndex = new HashMap<>(capacity);
    }
    public GameRoom(){
        this(12);
    }
    public int round(){
        return round;
    }
    public int capacity(){
        return capacity;
    }
    public Arena arena(){
        return this.arena;
    }


    public String roomId(){
        return this.distributionKey();
    }
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",capacity);
        this.properties.put("2",round);
        this.properties.put("3",this.index);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.capacity = ((Number)properties.getOrDefault("1",12)).intValue();
        this.round = ((Number)properties.getOrDefault("2",0)).intValue();
        this.index = (String)properties.getOrDefault("3","");
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
        portableWriter.writeInt("2",round);
        portableWriter.writeInt("3",capacity);
        portableWriter.writePortableArray("4",entries);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        this.distributionKey(portableReader.readUTF("1"));
        this.round = portableReader.readInt("2");
        entries = new GameEntry[portableReader.readInt("3")];
        for(Portable p : portableReader.readPortableArray("4")){
            GameEntry gameEntry = (GameEntry)p;
            entries[gameEntry.seatIndex]=gameEntry;
        }
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
    public void onTimer(Module.OnUpdate onUpdate){
        
    }
    public void setup(Arena arena){
        this.arena = arena;
        this.capacity = arena.capacity;
        this.duration = arena.duration;
    }
    public void load(){
        entries = new GameEntry[capacity];
        dataStore.list(new GameEntryQuery(this.distributionKey()),(ge)->{
            entries[ge.seatIndex]=ge;
            if(ge.occupied) joinIndex.put(ge.systemId,ge);
            return true;
        });
    }
    @Override
    public void update(){

    }
    public synchronized void join(String systemId){
        if(joinIndex.containsKey(systemId)) return;
        for(int i=0;i<capacity;i++){
            GameEntry e = entries[i];
            if(e!=null&&e.occupied) continue;
            if(e==null){
                e = new GameEntry(i);
                e.owner(this.distributionKey());
                this.dataStore.create(e);
                entries[i]=e;
            }
            e.systemId = systemId;
            e.occupied = true;
            this.dataStore.update(e);
            joinIndex.put(systemId,e);
            break;
        }
    }
    public synchronized boolean leave(String systemId){
        GameEntry rm = joinIndex.remove(systemId);
        if(rm!=null){
            rm.occupied = false;
            this.dataStore.update(rm);
        }
        return joinIndex.isEmpty();
    }
}