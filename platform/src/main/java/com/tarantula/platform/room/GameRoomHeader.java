package com.tarantula.platform.room;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Channel;
import com.icodesoftware.Connection;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.game.Arena;
import com.tarantula.game.GameZone;
import com.tarantula.game.Rating;

import java.io.IOException;
import java.util.*;

abstract public class GameRoomHeader extends RecoverableObject implements GameRoom {

    protected int channelId;
    protected int sessionId;
    protected int timeout;
    protected byte[] serverKey;
    protected Connection connection;

    protected int capacity;
    protected long duration;
    protected int round;
    protected int totalJoined;
    protected boolean started;
    protected Arena arena;

    protected HashMap<String,Entry> joinIndex;
    protected Entry[] entries;

    public int channelId(){
        return channelId;
    }
    public int sessionId(){
        return sessionId;
    }
    public int timeout(){return timeout;}
    public byte[] serverKey(){
        return serverKey;
    }
    public Connection connection(){
        return connection;
    }

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

    @Override
    public List<Entry> entries(){
        ArrayList<Entry> list = new ArrayList<>();
        joinIndex.forEach((k,e)->list.add(e));
        return list;
    }
    public GameRoomHeader(int capacity){
        this.capacity = capacity;
        this.round = 1;
        this.joinIndex = new HashMap<>(capacity);
        this.entries = new Entry[capacity];
    }

    @Override
    public void load(){
        //entries = new Entry[capacity];
        dataStore.list(new GameEntryQuery(this.distributionKey()),(ge)->{
            entries[ge.seat()]=ge;
            if(ge.occupied()) joinIndex.put(ge.systemId(),ge);
            return true;
        });
    }

    @Override
    public void setup(GameZone gameZone,Channel channel, Rating rating){
        this.arena = gameZone.arena(rating.arenaLevel);
        this.capacity = gameZone.capacity();
        this.duration = gameZone.roundDuration();
        if(channel==null) return;
        this.connection = channel.connection();
        this.channelId = channel.channelId();
        this.sessionId = channel.sessionId();
        this.serverKey = channel.serverKey();
        this.timeout = channel.connection().timeout();
    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",capacity);
        this.properties.put("2",round);
        this.properties.put("3",totalJoined);
        this.properties.put("4",started);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.capacity = ((Number)properties.getOrDefault("1",1)).intValue();
        this.round = ((Number)properties.getOrDefault("2",0)).intValue();
        this.totalJoined = ((Number)properties.getOrDefault("3",0)).intValue();
        this.started = (Boolean)properties.getOrDefault("4",false);
    }
    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("RoomId",distributionKey());
        jsonObject.addProperty("Capacity",capacity);
        jsonObject.addProperty("Duration",duration);
        jsonObject.addProperty("Round",round);
        if(connection!=null){
            jsonObject.addProperty("ChannelId",channelId);
            jsonObject.addProperty("SessionId",sessionId);
            jsonObject.addProperty("Timeout",timeout);
            jsonObject.addProperty("ServerKey",Base64.getEncoder().encodeToString(serverKey));
            jsonObject.add("_connection",connection.toJson());
        }
        if(entries==null) return jsonObject;
        JsonArray plist = new JsonArray();
        for(Entry ge : entries){
            if(ge==null) continue;
            plist.add(ge.toJson());
        }
        jsonObject.add("_players",plist);
        return jsonObject;
    }

    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeInt("1",round);
        portableWriter.writeUTF("2",bucket);
        portableWriter.writeUTF("3",oid);
        portableWriter.writeInt("4",capacity);
        portableWriter.writePortableArray("5",entries);
    }

    public void readPortable(PortableReader portableReader) throws IOException {
        this.round = portableReader.readInt("1");
        this.bucket = portableReader.readUTF("2");
        this.oid = portableReader.readUTF("3");
        this.entries = new Entry[portableReader.readInt("4")];
        for(Portable p : portableReader.readPortableArray("5")){
            Entry gameEntry = (Entry)p;
            entries[gameEntry.seat()] = gameEntry;
        }
    }

    public synchronized GameRoom join(String systemId,Listener listener){
        if(joinIndex.containsKey(systemId)) {
            listener.onUpdated(this,joinIndex.get(systemId));
            return view();
        };
        for(int i=0;i<capacity;i++){
            Entry e = entries[i];
            if(e!=null&&e.occupied()) continue;
            if(e==null){
                e = this.createEntry();
                e.seat(i);
                e.owner(this.distributionKey());
                this.dataStore.create(e);
                entries[i]=e;
            }
            e.systemId(systemId);
            e.occupied(true);
            e.seat(i);
            this.dataStore.update(e);
            joinIndex.put(systemId,e);
            break;
        }
        totalJoined++;
        started = full();
        this.dataStore.update(this);
        listener.onUpdated(this,joinIndex.get(systemId));
        return view();
    }

    public synchronized void leave(String systemId,Listener listener){
        Entry rm = joinIndex.remove(systemId);
        if(rm!=null){
            totalJoined--;
            rm.reset();
            this.dataStore.update(rm);
            this.dataStore.update(this);
        }
        listener.onUpdated(this,rm);
    }

    public synchronized GameRoom view(){
        GameRoom room = duplicate();
        if(room==null) return this;
        room.bucket(this.bucket);
        room.oid(this.oid);
        return room;
    }

    public boolean empty(){
        return totalJoined==0;
    }
    public boolean full(){
        return totalJoined==capacity;
    }
    public boolean started(){
        return started;
    }

    public synchronized void reset(){
        joinIndex.forEach((k,v)->{
            v.reset();
            this.dataStore.update(v);
        });
        joinIndex.clear();
        entries = new Entry[capacity];
        totalJoined = 0;
        round++;
        this.dataStore.update(this);
    }

    protected GameRoom duplicate(){
        return null;
    }
    protected GameRoom.Entry createEntry(){
        return new GameEntry();
    }

    @Override
    public String toString(){
        return "ROOM ["+distributionKey()+"] Capacity ["+capacity+"][ Total Joined ["+totalJoined+"] Round ["+round+"]";
    }
}
