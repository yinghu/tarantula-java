package com.tarantula.platform.room;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.protocol.Arena;
import com.icodesoftware.protocol.*;
import com.icodesoftware.Connection;
import com.icodesoftware.Session;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.SnowflakeKey;
import com.tarantula.cci.udp.UDPChannel;
import com.tarantula.game.GameArena;
import com.tarantula.game.GamePortableRegistry;
import com.tarantula.game.GameZone;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class GameRoomHeader extends RecoverableObject implements GameRoom {

    protected int channelId;
    protected int sessionId;
    protected int timeout;
    protected byte[] serverKey;
    protected Connection connection;

    protected int capacity;
    protected long duration;

    protected int joinsOnStart;
    protected long overtime;

    protected boolean dedicated;
    protected GameArena arena;
    protected long zoneId;
    protected int bucket;
    protected ConcurrentHashMap<Long,Entry> joinIndex;
    protected Entry[] entries;

    private GameServiceProvider gameModule;
    private ArrayBlockingQueue<Channel> pendingChannels;


    public int channelId(){
        return channelId;
    }
    public int sessionId(){
        return sessionId;
    }
    public int timeout(){return timeout;}

    public boolean dedicated(){
        return dedicated;
    }


    @Override
    public long roomId(){
        return this.distributionId();
    }

    public long zoneId(){
        return zoneId;
    }

    public int bucket(){
        return bucket;
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

    public Seat[] table(){
        return entries;
    }
    public GameRoomHeader(){
        this.onEdge = true;
        this.label = LABEL;
    }

    public GameRoomHeader(int capacity){
        this();
        this.capacity = capacity;
        this.joinIndex = new ConcurrentHashMap<>(capacity);
        this.entries = new Entry[capacity];
    }

    @Override
    public void load(){
        int[] created ={0};
        if(entries==null) entries = new Entry[capacity];
        dataStore.list(new GameEntryQuery(this.distributionId()),(ge)->{
            entries[ge.number()]=ge;
            created[0]++;
            return true;
        });
        if(created[0]==capacity) return;
        for(int i=0;i<capacity;i++){
            Entry entry = new GameEntry();
            entry.number(i);
            entry.occupied(false);
            entry.ownerKey(new SnowflakeKey(this.distributionId));
            if(!this.dataStore.create(entry)) throw new RuntimeException("cannot create room entry");
            entries[i]=entry;
        }
    }

    @Override
    public void setup(Channel channel){
        this.connection = channel.connection();
        this.channelId = channel.channelId();
        this.sessionId = channel.sessionId();
        this.serverKey = channel.serverKey();
        this.timeout = channel.connection().timeout();
        this.dedicated = true;
    }

    @Override
    public boolean read(DataBuffer buffer){
        this.capacity = buffer.readInt();
        return true;
    }
    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeInt(capacity);
        return true;
    }
    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("RoomId",distributionKey());
        jsonObject.addProperty("Capacity",capacity);
        jsonObject.addProperty("Duration",duration);
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

    public GameRoom join(Session session,RoomStub roomStub){
        Entry entry = null;
        synchronized (entries){
            for(int i=0;i<capacity;i++){
                if(entries[i].occupied() && entries[i].systemId()==session.systemId() && entries[i].stub()==session.stub()){
                    entry = entries[i];
                    break;
                }
                if(!entries[i].occupied() && entry==null){
                    entry = entries[i];
                }
            }
            if(entry.systemId()==0) entry.occupied(true);
        }
        entry.systemId(session.systemId());
        entry.stub(session.stub());
        this.dataStore.update(entry);
        return view();
    }

    public void leave(Session session){
        Entry entry = null;
        synchronized (entries){
            for(int i=0;i<capacity;i++){
                if(!entries[i].occupied()) continue;
                if(entries[i].systemId()==session.systemId() && entries[i].stub()==session.stub()){
                    entry = entries[i];
                    entry.occupied(false);
                    entry.systemId(0);
                    entry.stub(0);
                    break;
                }
            }
        }
        if(entry==null) return;
        this.dataStore.update(entry);
    }

    public GameRoom view(){
        return this;
    }



    public void reset(){
        if(pendingChannels!=null) pendingChannels.clear();
        joinIndex.clear();
        for(int i=0;i<capacity;i++){
            entries[i].reset();
            this.dataStore.update(entries[i]);
        }
        this.dataStore.update(this);
    }


    public void setup(GameServiceProvider gameServiceProvider,GameZone gameZone,boolean dedicated){
        this.capacity = gameZone.capacity();
        this.duration = gameZone.roundDuration();
        this.overtime = gameZone.roundOvertime();
        this.joinsOnStart = gameZone.joinsOnStart();
        this.dedicated = dedicated;
        this.gameModule = gameServiceProvider;
        this.zoneId = gameZone.distributionId();
        this.arena = gameZone.arena(1);
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
        ((UDPChannel)channel).register(session,this.gameModule,this.gameModule,this.gameModule,timeoutListener);
        return channel;
    }
    @Override
    public String toString(){
        return "ROOM ["+distributionKey()+"] Capacity ["+capacity+"] Channel ["+channelId+"]";
    }

    @Override
    public int getFactoryId() {
        return GamePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return GamePortableRegistry.PVE_ROOM_CID;
    }

}
