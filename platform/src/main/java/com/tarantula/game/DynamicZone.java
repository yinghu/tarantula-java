package com.tarantula.game;

import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.game.service.DynamicGameLobbySetup;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DynamicZone extends RecoverableObject implements GameZone {

    protected int levelMatch;
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
    protected CopyOnWriteArrayList<Listener> listeners;

    public DynamicZone(){
        this.arenaList = new ArrayList<>();
        this.levelIndex = new ConcurrentHashMap<>();
        this.stubIndex = new ConcurrentHashMap<>();
        this.listeners = new CopyOnWriteArrayList<>();
    }
    
    public DynamicZone(String name,String playMode,int levelMatch,int levelLimit,int roomCapacity,int joinsOnStart,long roundDuration){
        this();
        this.name = name;
        this.playMode = playMode;
        this.levelMatch = levelMatch;
        this.levelLimit = levelLimit;
        this.capacity = roomCapacity;
        this.joinsOnStart = joinsOnStart;
        this.roundDuration = roundDuration;
    }

    public Stub join(Session session,Rating rating){
        Stub _joined = stubIndex.get(session.systemId());
        if(_joined!=null) return _joined;
        Arena arena = levelIndex.get(rating.xpLevel>levelLimit?levelLimit:rating.xpLevel);
        Stub stub = new Stub();
        stub.distributionKey(session.systemId());
        stub.label(application.tag());
        dataStore.createIfAbsent(stub,true);
        stub.successful(true);
        stub.rating = rating;
        stub.arena = arena;
        stub.owner(session.systemId());
        stub.tag = application.tag();
        stub.tournamentEnabled = application.tournamentEnabled();
        rating.owner(session.systemId());
        GameRoom room = roomProxy.join(session,arena,rating);
        //setup after joining
        stub.joined = true;
        stub.roomId = room.roomId();
        dataStore.update(stub);
        this.applicationContext.log(stub.toString(),OnLog.WARN);
        stub.room = room;
        stub.offline = room.offline;
        stub.instance = room.instance;
        stubIndex.put(session.systemId(),stub);
        return stub;
    }
    public void update(String systemId){
        if(application.tournamentEnabled()){
            Stub stub = stubIndex.get(systemId);
            this.roomProxy.update(systemId,stub.instance);
        }
    }
    public void leave(String systemId){
        Stub stub = stubIndex.remove(systemId);
        stub.joined = false;
        this.dataStore.update(stub);
        this.applicationContext.log(stub.toString(),OnLog.WARN);
        roomProxy.leave(stub);
    }
    public void addArena(Arena arena){
        arenaList.add(arena);
    }
    public List<Arena> arenas(){
        return arenaList;
    }
    public Arena arena(int level){
        return levelIndex.get(level).copy();
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
    public int levelMatch(){
        return levelMatch;
    }
    public void levelMatch(int levelMatch){
        this.levelMatch = levelMatch;
    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",capacity);
        this.properties.put("2",roundDuration);
        this.properties.put("3",levelMatch);
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
        this.roundDuration = ((Number)properties.getOrDefault("2",roundDuration)).longValue();
        this.levelMatch = ((Number)properties.getOrDefault("3",levelMatch)).intValue();
        this.name = (String)properties.get("5");
        this.timestamp = ((Number)properties.getOrDefault("6",0)).longValue();
        this.levelLimit = ((Number)properties.getOrDefault("7",levelLimit)).intValue();
        this.joinsOnStart = ((Number)properties.getOrDefault("8",joinsOnStart)).intValue();
        this.playMode = (String)properties.get("9");
    }

    @Override
    public void update() {//local data store update
        arenaList.forEach((a)->{
            if(!this.dataStore.update(a)) this.dataStore.create(a);
        });
        this.timestamp = TimeUtil.toUTCMilliseconds(LocalDateTime.now());
        this.dataStore.update(this);
    }

    @Override
    public void registered(){
        listeners.forEach((l)->l.onLoaded(this));
    }
    @Override
    public void released(){
        listeners.forEach((l)->l.onRemoved(this));
    }
    @Override
    public void updated(ServiceContext serviceContext){//config sync callback
        this.applicationContext.log("zone updated->"+distributionKey(), OnLog.WARN);
        GameZone updated = new DynamicGameLobbySetup().loadGameZone(this.dataStore,this.distributionKey());
        reset(updated);
        listeners.forEach((l)->l.onUpdated(updated));
    }

    public <T extends Configurable> void registerListener(Listener<T> listener){
        listeners.add(listener);
    }
    public Descriptor descriptor(){
        return application;
    }
    public void descriptor(Descriptor descriptor){

    }
    @Override
    public void setup(ApplicationContext applicationContext) throws Exception{
        this.applicationContext = applicationContext;
        this.application = this.applicationContext.descriptor();
        if(levelLimit==0||levelLimit>10) levelLimit = 10;
        listArena();
        roomProxy.setup(applicationContext,this);
    }

    public boolean connected(){
        return !this.playMode.equals(PLAY_MODE_PVE);
    }

    public void roomProxy(RoomProxy roomProxy){
        this.roomProxy = roomProxy;
    }

    private void listArena(){
        if(arenaList.size()==0) return;
        int fi = levelLimit;//this.descriptor.capacity();
        for(Arena a : arenaList){
            if(a.disabled()) continue;
            if(a.level>0&&a.level<=levelLimit){
                levelIndex.put(a.level,a);
                if(a.level<fi) fi = a.level;
            }
        }
        //set 1 to max level count
        for(int i=1;i<this.levelLimit+1;i++){//max matching level
            Arena ex = levelIndex.get(i);
            if(ex==null){
                if(levelIndex.get(i-1)!=null) levelIndex.put(i,levelIndex.get(i-1));//fill with last one
                else levelIndex.put(i,levelIndex.get(fi));//fill header
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

    public boolean configureAndValidate(byte[] data){
        Map<String,Object> map = JsonUtil.toMap(data);
        this.roundDuration = ((Number)map.get("duration")).intValue()*60000;
        return true;
    }
}
