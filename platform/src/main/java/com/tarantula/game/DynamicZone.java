package com.tarantula.game;

import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.game.service.DynamicGameLobbySetup;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DynamicZone extends RecoverableObject implements GameZone {

    protected int levelMatch;
    protected String playMode;
    protected int arenaLimit;
    protected int capacity;

    protected int maxJoinsPerRoom;
    protected int joinsOnStart;
    protected long roundDuration;

    protected List<Arena> arenaList;
    protected ConcurrentHashMap<Integer,Arena> levelIndex;

    protected ApplicationContext applicationContext;
    protected Descriptor application;
    protected RoomProxy roomProxy;

    protected CopyOnWriteArrayList<Listener> listeners;

    public DynamicZone(){
        this.arenaList = new ArrayList<>();
        this.levelIndex = new ConcurrentHashMap<>();
        this.listeners = new CopyOnWriteArrayList<>();
    }
    
    public DynamicZone(String name,String playMode,int levelMatch,int arenaLimit,int capacity,int roomCapacity,int joinsOnStart,long roundDuration){
        this();
        this.name = name;
        this.playMode = playMode;
        this.levelMatch = levelMatch;
        this.arenaLimit = arenaLimit;
        this.capacity = capacity;
        this.maxJoinsPerRoom = roomCapacity;
        this.joinsOnStart = joinsOnStart;
        this.roundDuration = roundDuration;
    }

    public Stub join(Session session,Rating rating){
        return roomProxy.join(session,rating);
    }
    public void update(Session session, Stub stub, byte[] payload, Module.OnUpdate onUpdate){
        roomProxy.update(session,stub,payload,onUpdate);
    }
    public void leave(Stub stub){
        roomProxy.leave(stub);
    }
    public void list(Session session,Stub stub){
        roomProxy.list(session,stub);
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
    public int arenaLimit(){
        return arenaLimit;
    }
    public int capacity(){
        return capacity;
    }

    public void arenaLimit(int arenaLimit){
        this.arenaLimit = arenaLimit;
    }
    public void capacity(int capacity){
        this.capacity = capacity;
    }
    public int maxJoinsPerRoom(){
        return maxJoinsPerRoom;
    }
    public void maxJoinsPerRoom(int maxJoinsPerRoom){
        this.maxJoinsPerRoom = maxJoinsPerRoom;
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
        this.properties.put("7",this.arenaLimit);
        this.properties.put("8",this.joinsOnStart);
        this.properties.put("9",this.playMode);
        this.properties.put("10",this.disabled);
        this.properties.put("11",this.maxJoinsPerRoom);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.capacity = ((Number)properties.getOrDefault("1",capacity)).intValue();
        this.roundDuration = ((Number)properties.getOrDefault("2",roundDuration)).longValue();
        this.levelMatch = ((Number)properties.getOrDefault("3",levelMatch)).intValue();
        this.name = (String)properties.get("5");
        this.timestamp = ((Number)properties.getOrDefault("6",0)).longValue();
        this.arenaLimit = ((Number)properties.getOrDefault("7",arenaLimit)).intValue();
        this.joinsOnStart = ((Number)properties.getOrDefault("8",joinsOnStart)).intValue();
        this.playMode = (String)properties.get("9");
        this.disabled = (boolean)properties.getOrDefault("10",false);
        this.maxJoinsPerRoom = ((Number)properties.getOrDefault("11",maxJoinsPerRoom)).intValue();
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

    @Override
    public void setup(ApplicationContext applicationContext,GameLobby gameLobby){
        this.applicationContext = applicationContext;
        this.application = this.applicationContext.descriptor();
        listArena();
        roomProxy.setup(applicationContext,gameLobby,this);
    }

    public boolean connected(){
        return !this.playMode.equals(PLAY_MODE_PVE);
    }

    public void roomProxy(RoomProxy roomProxy){
        this.roomProxy = roomProxy;
    }
    public DataStore dataStore(){
        return this.dataStore;
    }
    private void listArena(){
        Collections.sort(arenaList,new ArenaComparator());
        int start = 1;
        for(Arena a : arenaList){
            if(a.disabled()) continue;
            a.owner(this.distributionKey());
            levelIndex.put(a.level,a);
            for(int i = start;i<a.level;i++){
                levelIndex.put(i,a);
            }
            start++;
        }
        if(start<arenaLimit){
            Arena lastArena = levelIndex.get(start-1);
            for(int i = start;i<=arenaLimit;i++){
                levelIndex.put(i,lastArena);
            }
        }
        //levelIndex.forEach((k,v)->{
            //applicationContext.log("Arena level ["+k+"] registered on ["+v.level+"]",OnLog.WARN);
        //});
    }
    private void reset(GameZone updated){
        arenaList.clear();
        for(Arena a : updated.arenas()){
            arenaList.add(a);
        }
        synchronized (this){//update local zone copy
            this.name = updated.name();
            this.capacity = updated.capacity();
            this.maxJoinsPerRoom = updated.maxJoinsPerRoom();
            this.joinsOnStart = updated.joinsOnStart();
            this.roundDuration = updated.roundDuration();
            this.arenaLimit = updated.arenaLimit();
            levelIndex.clear();
            listArena();
        }
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
    public boolean configureAndValidate(Map<String,Object> data){
        this.roundDuration = ((Number)data.get("duration")).intValue()*60000;
        return true;
    }
    public JsonObject toJson(){
        JsonObject jzon = new JsonObject();
        jzon.addProperty("name",name);
        jzon.addProperty("rank",application.accessRank());
        jzon.addProperty("playMode",playMode);
        jzon.addProperty("levelMatch",levelMatch);
        jzon.addProperty("capacity",capacity);
        return jzon;
    }
}
