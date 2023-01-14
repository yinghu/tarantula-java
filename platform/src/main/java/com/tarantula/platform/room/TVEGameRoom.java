package com.tarantula.platform.room;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Module;
import com.tarantula.game.Arena;
import com.tarantula.game.service.GameEntryQuery;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TVEGameRoom extends GameRoomHeader implements Portable {


    private HashMap<String,GameEntry> joinIndex;
    private GameEntry[] entries;

    public TVEGameRoom(int capacity){
        this.capacity = capacity;
        joinIndex = new HashMap<>(capacity);
    }
    public TVEGameRoom(){
        this(12);
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
        return PortableEventRegistry.TVE_ROOM_CID;
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

    public void setup(Arena arena){
        this.arena = arena;
        //this.capacity = arena.capacity;
        //this.duration = arena.duration;
    }
    public void load(){
        entries = new GameEntry[capacity];
        dataStore.list(new GameEntryQuery(this.distributionKey()),(ge)->{
            entries[ge.seatIndex]=ge;
            if(ge.occupied) joinIndex.put(ge.systemId,ge);
            return true;
        });
    }

    public synchronized TVEGameRoom join(String systemId,RoomListener roomListener){
        if(joinIndex.containsKey(systemId)) return duplicate();
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
        return duplicate();
    }
    public synchronized void leave(String systemId,RoomListener roomListener){
        GameEntry rm = joinIndex.remove(systemId);
        if(rm!=null){
            rm.occupied = false;
            this.dataStore.update(rm);
        }
    }
    public synchronized TVEGameRoom view(){
        return this.duplicate();
    }
    public synchronized String[] joined(){
        if(joinIndex.isEmpty()) return new String[0];
        String[] joined = new String[joinIndex.size()];
        int[] i={0};
        joinIndex.forEach((k,v)->{
            joined[i[0]]=v.systemId;
            i[0]++;
        });
        return joined;
    }
    private TVEGameRoom duplicate(){
        TVEGameRoom _room = new TVEGameRoom();
        _room.entries = new GameEntry[joinIndex.size()];
        joinIndex.forEach((k,e)->_room.entries[e.seatIndex]=e);
        _room.capacity = _room.entries.length;
        _room.round = this.round;
        _room.bucket(this.bucket);
        _room.oid(this.oid);
        return this;
    }
}