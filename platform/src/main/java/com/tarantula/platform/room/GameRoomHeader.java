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

    protected String playMode;
    protected int channelId;
    protected int sessionId;
    protected int timeout;
    protected byte[] serverKey;
    protected Connection connection;

    protected int capacity;
    protected long duration;
    protected int round;
    protected Arena arena;
    protected Channel channel;
    protected HashMap<String,GameEntry> joinIndex;
    protected GameEntry[] entries;

    public String playMode(){
        return this.playMode;
    }
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
    public Channel channel(){
        return this.channel;
    }
    public void channel(Channel channel){
        this.channel = channel;
    }

    public void load(){
        entries = new GameEntry[capacity];
        dataStore.list(new GameEntryQuery(this.distributionKey()),(ge)->{
            entries[ge.seatIndex]=ge;
            if(ge.occupied) joinIndex.put(ge.systemId,ge);
            return true;
        });
    }
    public String[] joined(){
        if(joinIndex.isEmpty()) return new String[0];
        String[] joined = new String[joinIndex.size()];
        int[] i={0};
        joinIndex.forEach((k,v)->{
            joined[i[0]]=v.systemId;
            i[0]++;
        });
        return joined;
    }
    public void setup(GameZone gameZone, Rating rating){
        this.arena = gameZone.arena(rating.arenaLevel);
        this.capacity = gameZone.capacity();
        this.duration = gameZone.roundDuration();
        this.playMode = gameZone.playMode();
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
        jsonObject.addProperty("PlayMode",playMode);
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
        portableWriter.writeInt("6",round);
        portableWriter.writeInt("7",capacity);
        portableWriter.writePortableArray("8",entries);
    }

    public void readPortable(PortableReader portableReader) throws IOException {
        this.round = portableReader.readInt("6");
        entries = new GameEntry[portableReader.readInt("7")];
        for(Portable p : portableReader.readPortableArray("8")){
            GameEntry gameEntry = (GameEntry)p;
            entries[gameEntry.seatIndex]=gameEntry;
        }
    }
}
