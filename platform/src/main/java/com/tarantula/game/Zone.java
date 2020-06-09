package com.tarantula.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.RecoverableObject;
import com.tarantula.platform.service.DeploymentServiceProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by yinghu lu on 5/27/2020.
 */
public class Zone extends RecoverableObject implements RoomListener,DataStore.Updatable{
    public Arena[] arenas = new  Arena[0];
    public String name;
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
    private ConcurrentLinkedDeque[] pendingMatch = {
            new ConcurrentLinkedDeque(),//0
            new ConcurrentLinkedDeque(),//1
            new ConcurrentLinkedDeque(),//2
            new ConcurrentLinkedDeque(),//3
            new ConcurrentLinkedDeque(),//4
            new ConcurrentLinkedDeque(),//5
            new ConcurrentLinkedDeque(),//6
            new ConcurrentLinkedDeque(),//7
            new ConcurrentLinkedDeque(),//8
            new ConcurrentLinkedDeque(),//9
            new ConcurrentLinkedDeque() //10
    };
    private ConcurrentLinkedDeque<Room> rQueue = pendingMatch[0];
    public ConcurrentHashMap<Integer,Arena> aMap = new ConcurrentHashMap<>();
    public Zone(){
        this.vertex = "Zone";
    }
    public Room room(Rating rating){
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
        listArena();
        for(int i=0;i<3;i++){
            Room room = new Room();
            room.start(capacity,roundDuration,playMode!=Room.OFF_LINE_MODE,this);
            rQueue.offer(room);
            rList.add(room);
            roomIndex.put(room.roomId,room);
        }
    }
    public void onTimer(Module.OnUpdate update){
        rList.forEach((r)->{
            r.onTimer(update);
        });
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
                mLevel = rating.level;//use lower level for matching
            }
        }
        synchronized (this){
            Arena match = aMap.get(mLevel);
            jsonObject.addProperty("arena",match.name());
            jsonObject.addProperty("capacity",capacity);
            jsonObject.addProperty("duration",roundDuration/1000);
            jsonObject.addProperty("overtime",overtime/1000);
        }
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
        Statistics.Entry statistics = this.gameServiceProvider.statistics(stub.owner()).entry(stub.stats.name);
        statistics.update(stub.stats.value).update();
        this.gameServiceProvider.leaderBoard(statistics.name()).onAllBoard(statistics);
    }
    @Override
    public void onTimeout(Room room){
        room.start(capacity,roundDuration,playMode!=Room.OFF_LINE_MODE,this);
        rQueue.addLast(room);
    }
    @Override
    public void onEnding(Room room) {
        if(playMode!=Room.OFF_LINE_MODE){
            this.deploymentServiceProvider.onEndedUDPConnection(room.connection().serverId());
        }
        for(Stub sb : room.playerList()){
            stubIndex.remove(sb.owner());
            Rating rating = gameServiceProvider.rating(sb.owner());
            rating.update(sb);
            rating.update();
            if(sb.rank==1){
                Statistics.Entry stat = this.gameServiceProvider.statistics(sb.owner()).entry("wc");
                stat.update(1).update();
                gameServiceProvider.leaderBoard("wc").onAllBoard(stat);
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
        synchronized (this){
            Arena match = aMap.get(mLevel);
            jo.addProperty("arena",match.name());
            jo.addProperty("capacity",capacity);
            jo.addProperty("duration",roundDuration/1000);
            jo.addProperty("overtime",overtime/1000);
        }
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
        this.properties.put("__c",capacity);
        this.properties.put("__d",roundDuration);
        this.properties.put("__o",overtime);
        this.properties.put("__p",playMode);
        this.properties.put("__n",name);
        //this.properties.put("__a",disabled);
        for(Arena a : arenas){
            this.properties.put(a.name(),a.level+","+a.xp+","+a.disabled());
        }
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.capacity = ((Number)properties.get("__c")).intValue();
        this.roundDuration = ((Number)properties.get("__d")).longValue();
        this.overtime = ((Number)properties.get("__o")).longValue();
        this.playMode = ((Number)properties.get("__p")).intValue();
        this.name = (String)properties.get("__n");
        //this.disabled =(Boolean)properties.get("__a");
        ArrayList<Arena> alist = new ArrayList<>();
        properties.forEach((k,v)->{
            if(!k.startsWith("__")){
                Arena arena = new Arena();
                arena.name(k);
                String[] lx = ((String)v).split(",");
                arena.level = Integer.parseInt(lx[0]);
                arena.xp = Double.parseDouble(lx[1]);
                arena.disabled(Boolean.parseBoolean(lx[2]));
                alist.add(arena);
            }
        });
        arenas = new Arena[alist.size()];
        arenas = alist.toArray(arenas);
        Arrays.sort(arenas,new ArenaComparator());
    }
    @Override
    public Recoverable.Key key(){
        return new AssociateKey(this.bucket,this.oid,this.vertex);
    }
    public String toPlayMode(){
        if(playMode == Room.DEDICATED_MODE){
            return "Dedicated";
        }
        else if(playMode==Room.INTEGRATED_MODE){
            return "Integrated";
        }
        else{
            return "Offline";
        }
    }
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("successful",true);
        jsonObject.addProperty("capacity",capacity);
        jsonObject.addProperty("duration",roundDuration/60000);
        jsonObject.addProperty("playMode",toPlayMode());
        JsonArray jds = new JsonArray();
        for(Arena a: arenas){
            JsonObject jd = new JsonObject();
            jd.addProperty("name",a.name());
            jd.addProperty("level",a.level);
            jd.addProperty("xp",a.xp);
            jd.addProperty("disabled",a.disabled());
            jds.add(jd);
        }
        jsonObject.add("levels",jds);
        return jsonObject;
    }
    private void listArena(){
        int fi = this.descriptor.capacity();
        for(Arena a : arenas){
            if(a.level>0&&a.level<=this.descriptor.capacity()){
                aMap.put(a.level,a);
                if(a.level<fi){
                    fi = a.level;
                }
            }
        }
        //set 1 to max level count
        for(int i=1;i<this.descriptor.capacity()+1;i++){//max matching level
            Arena ex = aMap.get(i);
            if(ex==null){
                if(aMap.get(i-1)!=null){
                    aMap.put(i,aMap.get(i-1));//fill with last one
                }
                else{
                    aMap.put(i,aMap.get(fi));//fill header
                }
            }
            //context.log("Add lobby ->"+mZone.get(i).tag()+" ->rank ["+mZone.get(i).accessRank()+"]",OnLog.WARN);
        }
    }
    public void reset(Zone updated){
        ArrayList<Arena> alist = new ArrayList<>();
        for(Arena a : updated.arenas){
            if(!a.disabled()){
                alist.add(a);
            }
        }
        this.arenas = new Arena[alist.size()];
        for(int i=0;i<this.arenas.length;i++){
            this.arenas[i]=alist.get(i);
        }
        synchronized (this){//update local zone copy
            this.name = updated.name;
            this.capacity = updated.capacity;
            this.roundDuration = updated.roundDuration;
            this.playMode = updated.playMode;
            aMap.clear();
            listArena();
        }
    }
}
