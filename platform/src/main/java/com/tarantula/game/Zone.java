package com.tarantula.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.game.service.Rating;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.RecoverableObject;
import com.tarantula.platform.service.DeploymentServiceProvider;
import com.tarantula.platform.util.JvmRNG;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by yinghu lu on 4/14/2020.
 */
public class Zone extends RecoverableObject implements RoomListener,Updatable{
    public Arena[] arenas = new  Arena[0];
    public int capacity =1;
    public long roundDuration =60000;
    public long overtime = Room.PENDING_TIME;
    public int playMode = Room.DEDICATED_MODE;
    public ConcurrentHashMap<String,Room> roomIndex;
    public ConcurrentHashMap<String,Stub> stubIndex;
    public DeploymentServiceProvider deploymentServiceProvider;
    public GameServiceProvider gameServiceProvider;
    public Descriptor descriptor;
    private CopyOnWriteArrayList<Room> rList = new CopyOnWriteArrayList<>();
    private ConcurrentLinkedDeque<Room> rQueue = new ConcurrentLinkedDeque<>();
    public Zone(){
        this.vertex = "Zone";
    }
    public Room room(){
        Room room = rQueue.poll();
        if(room==null){
            room = new Room();
            room.start(capacity,roundDuration,playMode!=Room.OFF_LINE_MODE,this);
            rList.add(room);
            roomIndex.put(room.roomId,room);
        }
        return room;
    }

    public void start(){
        for(int i=0;i<3;i++){
            Room room = new Room();
            room.start(capacity,roundDuration,playMode!=Room.OFF_LINE_MODE,this);
            rQueue.offer(room);
            rList.add(room);
            roomIndex.put(room.roomId,room);
        }
    }
    public void onTimer(Module.OnUpdate update){
        rList.forEach((r)->r.onTimer(update));
    }

    @Override
    public int getFactoryId() {
        return GamePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return GamePortableRegistry.ZONE_CID;
    }

    @Override
    public void onWaiting(Room room) {
        rQueue.addFirst(room);
    }
    @Override
    public void onLeaving(Stub stub){
        stubIndex.remove(stub.owner());
    }
    @Override
    public Connection onConnection(Room room){
        String cType = playMode==Room.INTEGRATED_MODE?"tarantula":descriptor.typeId();
        return deploymentServiceProvider.onUDPConnection(cType,room);
    }
    @Override
    public void onConnecting(Room room){
        this.deploymentServiceProvider.onStartedUDPConnection(room.connection().serverId(),roomSetting(room));
    }
    @Override
    public byte[] onStarting(Room room){
        JsonObject jsonObject = new JsonObject();
        int mLevel = 10;
        for(Stub stub : room.playerList()){
            Rating rating = gameServiceProvider.rating(stub.owner());
            if(rating.level<mLevel){
                mLevel = rating.level;
            }
        }
        jsonObject.addProperty("arena",arenas[mLevel].name);
        jsonObject.addProperty("capacity",capacity);
        jsonObject.addProperty("duration",roundDuration/1000);
        jsonObject.addProperty("overtime",overtime/1000);
        jsonObject.addProperty("totalJoined",room.totalJoined());
        jsonObject.addProperty("state",room.state());
        JsonArray ja = new JsonArray();
        for(Stub p : room.playerList()){
            JsonObject jb = new JsonObject();
            jb.addProperty("owner",p.owner());
            jb.addProperty("seat",p.seat);
            ja.add(jb);
        }
        jsonObject.add("playerList",ja);
        if(room.connection()!= null) {
            Connection connection = room.connection();
            JsonObject jcc = new JsonObject();
            jcc.addProperty("serverId", connection.serverId());
            jcc.addProperty("host",connection.host());
            jcc.addProperty("port",connection.port());
            jsonObject.add("connection", jcc);
        }
        return jsonObject.toString().getBytes();
    }
    @Override
    public void onUpdating(Stub stub){
        Statistics statistics = this.gameServiceProvider.statistics(stub.owner());
        statistics.entry(stub.stat.name()).value(stub.stat.value());
        statistics.update();
    }
    @Override
    public void onEnding(Room room) {
        this.deploymentServiceProvider.onEndedUDPConnection(room.connection().serverId());
        for(Stub sb : room.playerList()){
            stubIndex.remove(sb.owner());
            Rating rating = gameServiceProvider.rating(sb.owner());
            rating.update(sb);
            rating.update();
            if(sb.rank==1){
                LeaderBoard leaderBoard = gameServiceProvider.leaderBoard("wc");
                leaderBoard.onBoard(sb.owner(),rating.csw);
            }
        }
        room.start(capacity,roundDuration,playMode!=Room.OFF_LINE_MODE,this);
        rQueue.addLast(room);
    }
    //room setting on online play mode
    private byte[] roomSetting(Room room){
        JsonObject jo = new JsonObject();
        //match lower arena on player rating level
        int mLevel = 10;
        for(Stub stub : room.playerList()){
            Rating rating = gameServiceProvider.rating(stub.owner());
            if(rating.level<mLevel){
                mLevel = rating.level;
            }
        }
        jo.addProperty("arena",arenas[mLevel].name);
        jo.addProperty("capacity",capacity);
        jo.addProperty("duration",roundDuration/1000);
        jo.addProperty("overtime",overtime/1000);
        jo.addProperty("totalJoined",room.totalJoined());
        jo.addProperty("roomId",room.roomId);
        JsonArray ja = new JsonArray();
        for(Stub p : room.playerList()){
            JsonObject jb = new JsonObject();
            jb.addProperty("owner",p.owner());
            jb.addProperty("seat",p.seat);
            ja.add(jb);
        }
        jo.add("playerList",ja);
        return jo.toString().getBytes();
    }
    @Override
    public Map<String,Object> toMap(){
        for(Arena a : arenas){
            this.properties.put(a.name,a.level+","+a.xp+","+a.capacity+","+a.duration+","+a.playMode);
        }
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        ArrayList<Arena> alist = new ArrayList<>();
        properties.forEach((k,v)->{
            Arena arena = new Arena();
            arena.name = k;
            String[] lx = ((String)v).split(",");
            arena.level = Integer.parseInt(lx[0]);
            arena.xp = Double.parseDouble(lx[1]);
            arena.capacity = Integer.parseInt(lx[2]);
            arena.duration = Integer.parseInt(lx[3]);
            arena.playMode = Integer.parseInt(lx[4]);
            alist.add(arena);
        });
        arenas = new Arena[alist.size()];
        arenas = alist.toArray(arenas);
        Arrays.sort(arenas,new ArenaComparator());
    }
    @Override
    public Recoverable.Key key(){
        return new AssociateKey(this.bucket,this.oid,this.vertex);
    }
}
