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

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

abstract public class GameRoomHeader extends RecoverableObject implements GameRoom {

    protected int channelId;
    protected int sessionId;
    protected byte[] serverKey;
    protected Connection connection;

    protected int capacity;
    protected long duration;
    protected int round;
    protected Arena arena;
    protected Channel channel;
    protected HashMap<String,GameEntry> joinIndex;
    protected GameEntry[] entries;

    public int channelId(){
        return channelId;
    }
    public int sessionId(){
        return sessionId;
    }
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
        if(connection!=null){
            jsonObject.addProperty("channelId",channelId);
            jsonObject.addProperty("sessionId",sessionId);
            jsonObject.addProperty("serverKey",Base64.getEncoder().encodeToString(serverKey));
            jsonObject.add("connection",connection.toJson());
        }
        JsonArray plist = new JsonArray();
        for(GameEntry ge : entries){
            if(ge==null) continue;
            plist.add(ge.toJson());
        }
        jsonObject.add("onList",plist);
        return jsonObject;
    }

    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeInt("1",channelId);
        portableWriter.writeInt("2",sessionId);
        portableWriter.writeByteArray("3",serverKey);
        portableWriter.writePortable("4",(Portable)connection);
        portableWriter.writeUTF("5",this.distributionKey());
        portableWriter.writeInt("6",round);
        portableWriter.writeInt("7",capacity);
        portableWriter.writePortableArray("8",entries);
    }

    public void readPortable(PortableReader portableReader) throws IOException {
        channelId = portableReader.readInt("1");
        sessionId = portableReader.readInt("2");
        serverKey = portableReader.readByteArray("3");
        connection = portableReader.readPortable("4");
        this.distributionKey(portableReader.readUTF("5"));
        this.round = portableReader.readInt("6");
        entries = new GameEntry[portableReader.readInt("7")];
        for(Portable p : portableReader.readPortableArray("8")){
            GameEntry gameEntry = (GameEntry)p;
            entries[gameEntry.seatIndex]=gameEntry;
        }
    }
}
