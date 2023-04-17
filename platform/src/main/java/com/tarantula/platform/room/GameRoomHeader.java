package com.tarantula.platform.room;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.protocol.*;
import com.icodesoftware.Connection;
import com.icodesoftware.Session;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.cci.udp.UDPChannel;
import com.tarantula.game.Arena;
import com.tarantula.game.GameZone;
import com.tarantula.game.Rating;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

abstract public class GameRoomHeader extends RecoverableObject implements GameRoom {

    protected int channelId;
    protected int sessionId;
    protected int timeout;
    protected byte[] serverKey;
    protected Connection connection;

    protected int capacity;
    protected long duration;
    protected int round;
    protected int joinsOnStart;
    protected long overtime;

    protected int totalJoined;
    protected int totalLeft;
    protected boolean dedicated;
    protected Arena arena;

    protected ConcurrentHashMap<String,Entry> joinIndex;
    protected Entry[] entries;

    private GameModule gameModule;
    private ArrayBlockingQueue<Channel> pendingChannels;

    private long countdownTimer;
    private Entry placeHolder;
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
    public boolean dedicated(){
        return dedicated;
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
    public int joinsOnStart() {
        return joinsOnStart;
    }

    @Override
    public long overtime() {
        return overtime;
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
        this.joinIndex = new ConcurrentHashMap<>(capacity);
        this.entries = new Entry[capacity];
        this.placeHolder = createEntry();
    }

    @Override
    public void load(){
        int[] created ={0};
        dataStore.list(new GameEntryQuery(this.distributionKey()),(ge)->{
            entries[ge.seat()]=ge;
            if(ge.occupied()) joinIndex.put(ge.systemId(),ge);
            created[0]++;
            return true;
        });
        if(created[0]==capacity) return;
        for(int i=0;i<capacity;i++){
            Entry entry = createEntry();
            entry.seat(i);
            entry.occupied(false);
            entry.owner(this.distributionKey());
            if(!this.dataStore.create(entry)) throw new RuntimeException("cannot create room entry");
            entries[i]=entry;
        }
    }

    @Override
    public void setup(GameZone gameZone, Channel channel,Rating rating){
        this.arena = gameZone.arena(rating.arenaLevel);
        if(!dedicated) return;
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
        this.properties.put("4",totalLeft);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.capacity = ((Number)properties.getOrDefault("1",1)).intValue();
        this.round = ((Number)properties.getOrDefault("2",0)).intValue();
        this.totalJoined = ((Number)properties.getOrDefault("3",0)).intValue();
        this.totalLeft = ((Number)properties.getOrDefault("4",0)).intValue();

    }
    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("RoomId",distributionKey());
        jsonObject.addProperty("Capacity",capacity);
        jsonObject.addProperty("Duration",duration);
        jsonObject.addProperty("Round",round);
        jsonObject.addProperty("Dedicated",dedicated);
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

    public GameRoom join(String systemId,Listener listener){
        Entry entry = joinIndex.putIfAbsent(systemId,placeHolder);
        if(entry != null) return view();
        synchronized (entries){
            for(int i=0;i<capacity;i++){
                if(!entries[i].occupied()){
                    entry = entries[i];
                    entry.occupied(true);
                    entry.systemId(systemId);
                    totalJoined++;
                    break;
                }
            }
        }
        joinIndex.replace(systemId,entry);
        listener.onUpdated(this,entry);
        this.dataStore.update(entry);
        this.dataStore.update(this);
        return view();
    }

    public void leave(String systemId,Listener listener){
        Entry entry = joinIndex.remove(systemId);
        if(entry == null) return;
        synchronized (entries){
            entries[entry.seat()].reset();
            totalLeft++;
        }
        listener.onUpdated(this,entry);
        this.dataStore.update(entry);
        this.dataStore.update(this);
    }

    public GameRoom view(){
        GameRoom room = duplicate();
        if(room==null) return this;
        room.bucket(this.bucket);
        room.oid(this.oid);
        return room;
    }


    public boolean available(){
        return totalJoined < capacity;
    }
    public int totalJoined(){
        return totalJoined;
    }

    public int totalLeft(){
        return totalLeft;
    }

    public void reset(){
        if(gameModule!=null) gameModule.reset();
        if(pendingChannels!=null) pendingChannels.clear();
        joinIndex.clear();
        for(int i=0;i<capacity;i++){
            entries[i].reset();
            this.dataStore.update(entries[i]);
        }
        totalJoined = 0;
        totalLeft = 0;
        round++;
        countdownTimer = duration+overtime;
        this.dataStore.update(this);
    }

    public void close(){
        if(gameModule!=null) gameModule.close();
    }

    protected GameRoom duplicate(){
        return null;
    }
    protected GameRoom.Entry createEntry(){
        return new GameEntry();
    }

    public void setup(GameZone gameZone,GameModule gameModule,boolean dedicated){
        this.capacity = gameZone.capacity();
        this.duration = gameZone.roundDuration();
        this.overtime = gameZone.roundOvertime();
        this.joinsOnStart = gameZone.joinsOnStart();
        this.dedicated = dedicated;
        this.gameModule = gameModule;
        this.owner = gameZone.distributionKey();
        this.countdownTimer = this.duration+this.overtime;
    }

    public void setup(Channel[] channels){
        if(pendingChannels==null) pendingChannels = new ArrayBlockingQueue<>(channels.length);
        for(int i=0;i<channels.length;i++){
            pendingChannels.offer(channels[i]);
        }
        this.channelId = channels[0].channelId();
    }

    public Channel registerChannel(Session session,Session.TimeoutListener timeoutListener){
        Channel channel = pendingChannels.poll();
        ((UDPChannel)channel).register(session,this,this,this,timeoutListener);
        return channel;
    }
    @Override
    public String toString(){
        return "ROOM ["+distributionKey()+"] Capacity ["+capacity+"][ Total Joined ["+totalJoined+"] Round ["+round+"] Channel ["+channelId+"]";
    }

    public byte[] onRequest(Session session,MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer){
        return gameModule.onRequest(session,messageHeader,messageBuffer);
    }


    public void onAction(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer, UDPEndpointServiceProvider.RelayListener callback){
        gameModule.onAction(messageHeader,messageBuffer,callback);
    }

    @Override
    public void onValidated(Channel channel) {
        gameModule.onValidated(channel);
    }


    @Override
    public void onJoined(Channel channel) {
       gameModule.onJoined(channel);
    }

    @Override
    public void onLeft(Channel channel) {
       gameModule.onLeft(channel);
    }

    @Override
    public void onUpdated(GameServiceProvider gameServiceProvider, byte[] payload){
        gameModule.update(gameServiceProvider,payload);
    }

    @Override
    public void onCountdown(long delta){
        countdownTimer -= delta;
        gameModule.countdown(countdownTimer);
    }

}
