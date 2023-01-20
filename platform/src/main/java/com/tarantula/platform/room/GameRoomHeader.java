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
import com.tarantula.game.service.GameEntryQuery;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

abstract public class GameRoomHeader extends RecoverableObject implements GameRoom {

    protected int channelId;
    protected int sessionId;
    protected int timeout;
    protected byte[] serverKey;
    protected Connection connection;

    protected int capacity;
    protected long duration;
    protected int round;
    protected Arena arena;

    protected HashMap<String,GameEntry> joinIndex;
    protected GameEntry[] entries;

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

    public GameRoomHeader(int capacity){
        this.capacity = capacity;
    }

    public void load(){
        entries = new GameEntry[capacity];
        dataStore.list(new GameEntryQuery(this.distributionKey()),(ge)->{
            entries[ge.seatIndex]=ge;
            if(ge.occupied) joinIndex.put(ge.systemId,ge);
            return true;
        });
    }

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
        for(GameEntry ge : entries){
            if(ge==null) continue;
            plist.add(ge.toJson());
        }
        jsonObject.add("_players",plist);
        return jsonObject;
    }

    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeInt("1",round);
        portableWriter.writeInt("2",capacity);
        portableWriter.writePortableArray("3",entries);
    }

    public void readPortable(PortableReader portableReader) throws IOException {
        this.round = portableReader.readInt("1");
        entries = new GameEntry[portableReader.readInt("2")];
        for(Portable p : portableReader.readPortableArray("3")){
            GameEntry gameEntry = (GameEntry)p;
            entries[gameEntry.seatIndex]=gameEntry;
        }
    }
    public synchronized GameRoom join(String systemId){
        if(joinIndex.containsKey(systemId)) {
            return view();
        };
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
            e.seatIndex = i;
            this.dataStore.update(e);
            joinIndex.put(systemId,e);
            break;
        }
        return view();
    }
    public synchronized void leave(String systemId){
        GameEntry rm = joinIndex.remove(systemId);
        if(rm!=null){
            rm.occupied = false;
            this.dataStore.update(rm);
        }
    }
    public synchronized GameRoom view(){
        GameRoom room = duplicate();
        if(room==null) return this;
        room.bucket(this.bucket);
        room.oid(this.oid);
        return room;
    }
    protected GameRoom duplicate(){
        return null;
    }
}
