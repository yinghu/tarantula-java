package com.tarantula.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.protocol.DataBuffer;
import com.icodesoftware.protocol.MessageHandler;
import com.icodesoftware.service.DeployService;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.RecoverableObject;
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
public class Zone extends RecoverableObject implements RoomListener, DataStore.Updatable,Configurable{
    public String subscription;
    public List<Arena> arenas = new ArrayList<>();
    public String name;
    public int capacity = 1;
    private static int SOLO_CAPACITY = 1;
    public long roundDuration =60000;
    public long overtime = Room.PENDING_TIME;
    public int playMode = Room.INTEGRATED_MODE;
    public int joinsOnStart = 1;
    public ConcurrentHashMap<String,Stub> stubIndex;
    public DeploymentServiceProvider deploymentServiceProvider;
    public GameServiceProvider gameServiceProvider;
    public Descriptor descriptor;
    public int levelLimit;
    public int levelUpBase;
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
                gameServiceProvider.addRoom(matched);
            }
            synchronized (this) {
                Arena _ma = aMap.get(rating.xpLevel).copy();
                matched.reset(_ma.capacity>0?_ma.capacity:this.capacity,_ma.joinsOnStart>0?_ma.joinsOnStart:this.joinsOnStart,_ma.duration>0?_ma.duration:this.roundDuration, playMode != Room.OFF_LINE_MODE, levelLimit, _ma);
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
            gameServiceProvider.addRoom(room);
        }
        synchronized (this){
            Arena _ma = this.aMap.get(rating.xpLevel).copy();
            room.reset(SOLO_CAPACITY,SOLO_CAPACITY,_ma.duration>0?_ma.duration:this.roundDuration,false,levelLimit,_ma);
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
            gameServiceProvider.addRoom(room);
        }
    }
    public void onTimer(Module.OnUpdate update){
        rList.forEach((r)->{
            PendingUpdate delta = r.onTimer();
            if(delta!=null){
                update.on(r.connection(),delta.label,delta.pending.toArray());
            }
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
    public void onJoining(Room room){
        pendingMatch[room.arena().level].offer(room);
    }
    @Override
    public void onLeaving(Room room,Stub stub){
        stubIndex.computeIfPresent(stub.owner(),(k,v)->{
            if(v.roomId.equals(stub.roomId)){
                return null;//remove
            }
            else{
                return v;//keep
            }
        });
        if(room.totalJoined()==0){
            pendingMatch[room.arena().level].remove(room);
            rQueue.addFirst(room);//add first join queue
        }
    }
    @Override
    public Connection onConnecting(Room room){
        Connection connection;
        if(room.connection()==null||room.connection().disabled()){
            connection = this.deploymentServiceProvider.onConnection(descriptor.typeId());
        }
        else{
            connection = room.connection();
        }
        if(connection!=null){
           DataBuffer spec = roomSetting(room);
           //spec.putLong(connection.connectionId());
           this.deploymentServiceProvider.registerPostOffice().onConnection(connection).send(MessageHandler.GAME_SPEC+"/true",spec.toArray());
        }
        return connection;
    }

    @Override
    public PendingUpdate onStarting(Room room){
        //game started
        DataBuffer dataBuffer = new DataBuffer();
        //dataBuffer.putLong(room.connection().connectionId());
        dataBuffer.putUTF8("starting");
        return new PendingUpdate(MessageHandler.GAME_START+"/true",dataBuffer);
    }
    @Override
    public PendingUpdate onClosing(Room room){
        //game closing
        pendingMatch[room.arena().level].remove(room);
        DataBuffer dataBuffer = new DataBuffer();
        //dataBuffer.putLong(room.connection().connectionId());
        dataBuffer.putUTF8("closing");
        return new PendingUpdate(MessageHandler.GAME_CLOSING+"/true",dataBuffer);
    }
    @Override
    public PendingUpdate onOverTiming(Room room){
        DataBuffer dataBuffer = new DataBuffer();
        //dataBuffer.putLong(room.connection().connectionId());
        dataBuffer.putUTF8("overtime");
        return new PendingUpdate(MessageHandler.GAME_OVERTIME+"/true",dataBuffer);
    }
    public PendingUpdate onEnding(Room room) {
        DataBuffer dataBuffer = new DataBuffer();
        //dataBuffer.putLong(room.connection().connectionId());
        dataBuffer.putUTF8("ending");
        return  new PendingUpdate(MessageHandler.GAME_CLOSE+"/true",dataBuffer);
    }
    @Override
    public PendingUpdate onEnded(Room room){
        clearRoom(room);
        DataBuffer dataBuffer = new DataBuffer();
        //dataBuffer.putLong(room.connection().connectionId());
        dataBuffer.putUTF8("ended");
        return new PendingUpdate(MessageHandler.GAME_END+"/true",dataBuffer);
    }

    @Override
    public PendingUpdate onTimeout(Room room){
        clearRoom(room);
        if(room.connection()==null||room.connection().disabled()){
            return null;
        }
        DataBuffer dataBuffer = new DataBuffer();
        //dataBuffer.putLong(room.connection().connectionId());
        dataBuffer.putUTF8("ending");
        return new PendingUpdate(MessageHandler.GAME_JOIN_TIMEOUT+"/true",dataBuffer);
    }
    private void clearRoom(Room room){
        for(Stub stub : room.playerList()){
            if(stub.owner()!=null){
                stubIndex.remove(stub.owner());
            }
        }
        pendingMatch[room.arena().level].remove(room);
        room.reset();
        rQueue.addLast(room);
    }
    //room setting on online play mode
    private DataBuffer roomSetting(Room room){
        DataBuffer dataBuffer = new DataBuffer();
        Arena match = room.arena();
        dataBuffer.putInt(match.level);
        dataBuffer.putInt(room.capacity());
        dataBuffer.putLong(room.duration());
        dataBuffer.putLong(room.overtime());
        dataBuffer.putUTF8(room.roomId);
        dataBuffer.putUTF8(subscription);
        return dataBuffer;
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
        this.properties.put("8",this.joinsOnStart);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.capacity = ((Number)properties.getOrDefault("1",capacity)).intValue();
        this.joinsOnStart = ((Number)properties.getOrDefault("8",capacity)).intValue();
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
        if(playMode==Room.INTEGRATED_MODE){
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
        jsonObject.addProperty("joinsOnStart",joinsOnStart);
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
            this.joinsOnStart = updated.joinsOnStart;
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
        DeployService deployService = serviceContext.clusterProvider(Distributable.DATA_SCOPE).deployService();
        Zone zone = new Zone();
        zone.distributionKey(descriptor.distributionKey());
        byte[] _data = deployService.load(this.dataStore.name(),zone.distributionKey().getBytes());
        zone.fromMap(SystemUtil.toMap(_data));
        for(int i=1;i<descriptor.capacity()+1;i++){
            Arena a = new Arena(zone.bucket(),zone.oid(),i);
            _data = deployService.load(dataStore.name(),a.distributionKey().getBytes());
            if(_data!=null){
                a.fromMap(SystemUtil.toMap(_data));
                if(!a.disabled()){//skip disabled
                    zone.arenas.add(a);
                }
            }
        }
        this.listener.onUpdated(zone);
    }

    public void onStatistics(String systemId,String category,double delta){
        Statistics statistics = gameServiceProvider.statistics(systemId);
        Statistics.Entry entry = statistics.entry(category);
        entry.update(delta).update();
        LeaderBoard ldb = gameServiceProvider.leaderBoard(category);
        ldb.onAllBoard(entry);
    }
    public void onRating(Stub stub,int rankUpBase){
        Rating rating = this.gameServiceProvider.rating(stub.owner());
        rating.update(stub,rankUpBase,levelUpBase);
        rating.update();
    }
}
