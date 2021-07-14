package com.tarantula.game;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.Descriptor;
import com.icodesoftware.Module;
import com.icodesoftware.OnLog;
import com.icodesoftware.Recoverable;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.game.service.DynamicLobbySetup;
import com.tarantula.platform.AssociateKey;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DynamicZone extends RecoverableObject implements GameZone {

    protected String playMode;
    protected int levelLimit;
    protected int capacity;
    protected int joinsOnStart;
    protected long roundDuration;

    protected List<Arena> arenaList;
    protected ConcurrentHashMap<Integer,Arena> levelIndex;

    protected ApplicationContext applicationContext;
    protected Descriptor application;
    protected RoomProxy roomProxy;
    protected ConcurrentHashMap<String,Stub> stubIndex;
    public DynamicZone(){
        this.label = "Zone";
        this.arenaList = new ArrayList<>();
        this.levelIndex = new ConcurrentHashMap<>();
        this.stubIndex = new ConcurrentHashMap<>();
        this.joinsOnStart = DEFAULT_JOINS_ON_START;
        this.levelLimit = DEFAULT_LEVEL_COUNT;
        this.roundDuration = DEFAULT_ROUND_DURATION;
        this.capacity = PVE_MAX_ROOM_CAPACITY;
    }
    
    public DynamicZone(String name,String playMode){
        this();
        this.name = name;
        this.playMode = playMode;
        if(playMode.equals(PLAY_MODE_PVP)){
            this.capacity = PVP_MAX_ROOM_CAPACITY;
        }
        else if(playMode.equals(PLAY_MODE_TVE)){
            this.capacity = TVE_MAX_ROOM_CAPACITY;
        }
        else if(playMode.equals(PLAY_MODE_TVT)){
            this.capacity = TVT_MAX_ROOM_CAPACITY;
        }
    }

    public Stub join(Rating rating){
        Stub _joined = stubIndex.get(rating.distributionKey());
        if(_joined!=null){
            return _joined;
        }
        Arena arena = levelIndex.get(rating.xpLevel);
        Stub stub = roomProxy.join(arena,rating);
        stub.tag = application.tag();
        stub.capacity = capacity;
        stubIndex.put(rating.distributionKey(),stub);
        return stub;
    }
    public void leave(String systemId){
        stubIndex.remove(systemId);
    }
    public void addArena(Arena arena){
        arenaList.add(arena);
    }
    public List<Arena> arenas(){
        return arenaList;
    }
    public String playMode(){
        return playMode;
    }
    public int levelLimit(){
        return levelLimit;
    }
    public int capacity(){
        return capacity;
    }

    public void levelLimit(int levelLimit){
        this.levelLimit = levelLimit;
    }
    public void capacity(int capacity){
        this.capacity = capacity;
    }
    public int joinsOnStart(){
        return joinsOnStart;
    }
    public void joinsOnStart(int joinsOnStart){
        this.joinsOnStart = joinsOnStart;
    }

    public long roundDuration(){
        return roundDuration;
    }
    public void roundDuration(long roundDuration){
        this.roundDuration = roundDuration;
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
    }
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",capacity);
        this.properties.put("2",roundDuration);
        //this.properties.put("3",overtime);
        this.properties.put("5",name);
        this.properties.put("6",this.timestamp);
        this.properties.put("7",this.levelLimit);
        this.properties.put("8",this.joinsOnStart);
        this.properties.put("9",this.playMode);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.capacity = ((Number)properties.getOrDefault("1",capacity)).intValue();
        this.joinsOnStart = ((Number)properties.getOrDefault("8",capacity)).intValue();
        this.roundDuration = ((Number)properties.getOrDefault("2",roundDuration)).longValue();
        //this.overtime = ((Number)properties.getOrDefault("3",overtime)).longValue();
        this.name = (String)properties.get("5");
        this.timestamp = ((Number)properties.getOrDefault("6",0)).longValue();
        this.levelLimit = ((Number)properties.getOrDefault("7",levelLimit)).intValue();
        this.playMode = (String)properties.get("9");
    }
    @Override
    public void update() {//local data store update
        arenaList.forEach((a)->{
            if(!this.dataStore.update(a)){//failed if no key associated
                this.dataStore.create(a);
            }
        });
        this.timestamp = TimeUtil.toUTCMilliseconds(LocalDateTime.now());
        this.dataStore.update(this);
    }
    @Override
    public void update(ServiceContext serviceContext){//config sync callback
        this.applicationContext.log("zone updated->"+distributionKey(), OnLog.WARN);
        GameZone updated = new DynamicLobbySetup().load(serviceContext,application);
        reset(updated);
    }

    @Override
    public void start(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.application = this.applicationContext.descriptor();
        if(levelLimit==0||levelLimit>application.capacity()){
            levelLimit = this.application.capacity();
        }
        listArena();
    }
    public boolean connected(){
        return !this.playMode.equals(PLAY_MODE_PVE);
    }
    public void roomProxy(RoomProxy roomProxy){
        this.roomProxy = roomProxy;
    }
    private void listArena(){
        if(arenaList.size()==0){
            return;
        }
        int fi = levelLimit;//this.descriptor.capacity();
        for(Arena a : arenaList){
            if(a.disabled()){
                continue;
            }
            if(a.level>0&&a.level<=levelLimit){
                levelIndex.put(a.level,a);
                if(a.level<fi){
                    fi = a.level;
                }
            }
        }
        //set 1 to max level count
        for(int i=1;i<this.levelLimit+1;i++){//max matching level
            Arena ex = levelIndex.get(i);
            if(ex==null){
                if(levelIndex.get(i-1)!=null){
                    levelIndex.put(i,levelIndex.get(i-1));//fill with last one
                }
                else{
                    levelIndex.put(i,levelIndex.get(fi));//fill header
                }
            }
        }
    }
    private void reset(GameZone updated){
        arenaList.clear();
        for(Arena a : updated.arenas()){
            arenaList.add(a);
        }
        synchronized (this){//update local zone copy
            this.name = updated.name();
            this.capacity = updated.capacity();
            this.joinsOnStart = updated.joinsOnStart();
            this.roundDuration = updated.roundDuration();
            this.levelLimit = updated.levelLimit();
            levelIndex.clear();
            listArena();
        }
    }
    public void onTimer(Module.OnUpdate onUpdate){
        roomProxy.onTimer(onUpdate);
    }
    @Override
    public int getFactoryId() {
        return GamePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return GamePortableRegistry.GAME_ZONE_CID;
    }
}
