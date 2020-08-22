package com.tarantula.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.RecoverableObject;
import com.tarantula.platform.service.DeploymentServiceProvider;
import com.tarantula.platform.service.ServiceContext;
import com.tarantula.platform.util.SystemUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * updated by yinghu lu on 6/9/2020.
 */
public class Zone extends RecoverableObject implements RoomListener,DataStore.Updatable,Configurable{
    public List<Arena> arenas = new ArrayList<>();
    public String name;
    public int capacity =1;
    private static int SOLO_CAPACITY = 1;
    public long roundDuration =60000;
    public long overtime = Room.PENDING_TIME;
    public int playMode = Room.DEDICATED_MODE;
    public ConcurrentHashMap<String,Room> roomIndex;
    public ConcurrentHashMap<String,Stub> stubIndex;
    public DeploymentServiceProvider deploymentServiceProvider;
    public GameServiceProvider gameServiceProvider;
    public Descriptor descriptor;
    public int levelLimit;
    private CopyOnWriteArrayList<Room> rList = new CopyOnWriteArrayList<>();

    private ConcurrentLinkedDeque<Room>[] pendingMatch;
    private ConcurrentLinkedDeque<Room> rQueue;//assigned to pendingMatch 0

    public ConcurrentHashMap<Integer,Arena> aMap = new ConcurrentHashMap<>();

    private Configurable.Listener listener;
    public Zone(){
        this.label = "Zone";
    }
    public Room match(Rating rating){
        //level down matching
        Room matched = null;
        if(rating.xpLevel>levelLimit){//downgrade xp level and rank up after this round
            rating.xpLevel=levelLimit;
        }
        for(int lx = rating.xpLevel;lx>0;lx--){
            matched = pendingMatch[lx].poll();
            if(matched!=null){//matched
                break;
            }
        }
        if(matched!=null){
            return matched;
        }
        else{
            matched = rQueue.poll();
            if(matched==null){
                matched= new Room();
                matched.start(this);
                rList.add(matched);
                roomIndex.put(matched.roomId,matched);
            }
            synchronized (this) {
                Arena _ma = aMap.get(rating.xpLevel).copy();
                matched.reset(_ma.capacity>0?_ma.capacity:this.capacity,_ma.duration>0?_ma.duration:this.roundDuration, playMode != Room.OFF_LINE_MODE, levelLimit, _ma);
            }
            return matched;
        }
    }
    public Room solo(Rating rating){//always single player offline mode
        if(rating.xpLevel>levelLimit){//downgrade xp level and rank up after this round
            rating.xpLevel=levelLimit;
        }
        Room room = rQueue.poll();
        if(room==null){
            room= new Room();
            room.start(this);
            rList.add(room);
            roomIndex.put(room.roomId,room);
        }
        synchronized (this){
            Arena _ma = this.aMap.get(rating.xpLevel).copy();
            room.reset(SOLO_CAPACITY,_ma.duration>0?_ma.duration:this.roundDuration,false,levelLimit,_ma);
        }
        return room;
    }
    public void start(){
        //always to start max match queue to avoid level limit refresh
        if(levelLimit==0){//assign default limit from descriptor capacity
            levelLimit = this.descriptor.capacity();
        }
        pendingMatch = new ConcurrentLinkedDeque[descriptor.capacity()+1];
        for(int i=0;i<descriptor.capacity()+1;i++){
            pendingMatch[i]=new ConcurrentLinkedDeque<>();
        }
        rQueue = pendingMatch[0];
        listArena();
        for(int i=0;i<3;i++){
            Room room = new Room();
            room.start(this);
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

    //@Override
    public void onWaiting(Room room) {
        if(room.totalJoined()>0){
            pendingMatch[room.arena().level].offer(room);
        }else{
            rQueue.addFirst(room);//add first join queue
        }
    }
    @Override
    public void onLeaving(Stub stub){
        stubIndex.computeIfPresent(stub.owner(),(k,v)->{
            if(v.roomId.equals(stub.roomId)){
                return null;//remove
            }
            else{
                return v;//keep
            }
        });
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
        Arena match = room.arena();
        jsonObject.addProperty("level",match.level);
        jsonObject.addProperty("arena",match.name());
        jsonObject.addProperty("capacity",room.capacity());
        jsonObject.addProperty("duration",room.duration()/1000);
        jsonObject.addProperty("overtime",room.overtime()/1000);
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
        room.start(this);
        rQueue.addLast(room);
    }
    @Override
    public void onEnding(Room room) {
        if(!room.offline()){
            this.deploymentServiceProvider.onEndedUDPConnection(room.connection().serverId());
        }
        for(Stub sb : room.playerList()){
            this.onLeaving(sb);
            Rating rating = sb.rating;//gameServiceProvider.rating(sb.owner());
            rating.update(sb,room.rankUpBase(),room.arena().xp);
            rating.update();
            if(sb.rank==1){
                Statistics.Entry stat = this.gameServiceProvider.statistics(sb.owner()).entry("wc");
                stat.update(1).update();
                gameServiceProvider.leaderBoard("wc").onAllBoard(stat);
            }
        }
        room.start(this);
        rQueue.addLast(room);
    }
    //room setting on online play mode
    private byte[] roomSetting(Room room){
        JsonObject jo = new JsonObject();
        Arena match = room.arena();
        jo.addProperty("level",match.level);
        jo.addProperty("arena",match.name());
        jo.addProperty("capacity",room.capacity());
        jo.addProperty("duration",room.duration()/1000);
        jo.addProperty("overtime",room.overtime()/1000);
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
        this.properties.put("1",capacity);
        this.properties.put("2",roundDuration);
        this.properties.put("3",overtime);
        this.properties.put("4",playMode);
        this.properties.put("5",name);
        this.properties.put("6",this.timestamp);
        this.properties.put("7",this.levelLimit);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.capacity = ((Number)properties.getOrDefault("1",capacity)).intValue();
        this.roundDuration = ((Number)properties.getOrDefault("2",roundDuration)).longValue();
        this.overtime = ((Number)properties.getOrDefault("3",overtime)).longValue();
        this.playMode = ((Number)properties.getOrDefault("4",playMode)).intValue();
        this.name = (String)properties.get("5");
        this.timestamp = ((Number)properties.getOrDefault("6",0)).longValue();
        this.levelLimit = ((Number)properties.getOrDefault("7",levelLimit)).intValue();
    }
    @Override
    public Recoverable.Key key(){
        return new AssociateKey(this.bucket,this.oid,this.label);
    }
    @Override
    public String distributionKey() {
        if(this.bucket!=null&&this.oid!=null){
            return new StringBuffer(this.bucket).append(Recoverable.PATH_SEPARATOR).append(oid).append(Recoverable.PATH_SEPARATOR).append(label).toString();
        }
        else{
            return null;
        }
    }
    @Override
    public void distributionKey(String distributionKey) {
        String[] klist = distributionKey.split(Recoverable.PATH_SEPARATOR);
        this.bucket = klist[0];
        this.oid = klist[1];
        //this.label = klist[2];
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
        if(arenas.size()==0){
            return;
        }
        int fi = levelLimit;//this.descriptor.capacity();
        for(Arena a : arenas){
            if(a.level>0&&a.level<=levelLimit){
                aMap.put(a.level,a);
                if(a.level<fi){
                    fi = a.level;
                }
            }
        }
        //set 1 to max level count
        for(int i=1;i<this.levelLimit+1;i++){//max matching level
            Arena ex = aMap.get(i);
            if(ex==null){
                if(aMap.get(i-1)!=null){
                    aMap.put(i,aMap.get(i-1));//fill with last one
                }
                else{
                    aMap.put(i,aMap.get(fi));//fill header
                }
            }
        }
    }
    public void reset(Zone updated){
        arenas.clear();
        for(Arena a : updated.arenas){
            arenas.add(a);
        }
        synchronized (this){//update local zone copy
            this.name = updated.name;
            this.capacity = updated.capacity;
            this.roundDuration = updated.roundDuration;
            this.playMode = updated.playMode;
            this.levelLimit = updated.levelLimit;
            aMap.clear();
            listArena();
        }
    }
    public void update() {
        arenas.forEach((a)->{
            if(!this.dataStore.update(a)){//failed if no key associated
                this.dataStore.create(a);
            }
        });
        this.timestamp = SystemUtil.toUTCMilliseconds(LocalDateTime.now());
        this.dataStore.update(this);
    }
    public void registerListener(Listener listener){
        this.listener = listener;
    }
    public void update(ServiceContext serviceContext){
        this.listener.onUpdated(this);
    }
}
