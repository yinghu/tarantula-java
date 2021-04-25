package com.tarantula.game;

import com.icodesoftware.*;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.AssociateKey;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


abstract public class Zone extends RecoverableObject implements Configurable, DataStore.Updatable {

    public static final String PVE = "pve"; //player versus computer
    public static final String PVP = "pvp"; //player versus player
    public static final String TVE = "tve"; //team versus computer
    public static final String TVT = "tvt"; //team versus team

    public static final int DEFAULT_LEVEL_COUNT = 10;
    public static final int DEFAULT_LEVEL_UP_BASE = 1000;

    public List<Arena> arenas = new ArrayList<>();
    public ConcurrentHashMap<Integer,Arena> aMap = new ConcurrentHashMap<>();
    public String name;
    public int levelLimit;
    public String mode;
    public int capacity = 1;
    public long roundDuration =60000;
    public long overtime = Room.PENDING_TIME;
    public int playMode = Room.INTEGRATED_MODE;
    public int joinsOnStart = 1;

    public Descriptor descriptor;
    public Configurable.Listener listener;
    public ConcurrentHashMap<String,Stub> stubIndex;

    public Zone(){
        this.label = "Zone";
    }


    abstract public Stub join(Rating rating);

    abstract public void update(ServiceContext serviceContext);

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",capacity);
        this.properties.put("2",roundDuration);
        this.properties.put("3",overtime);
        this.properties.put("5",name);
        this.properties.put("6",this.timestamp);
        this.properties.put("7",this.levelLimit);
        this.properties.put("8",this.joinsOnStart);
        this.properties.put("9",this.index);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.capacity = ((Number)properties.getOrDefault("1",capacity)).intValue();
        this.joinsOnStart = ((Number)properties.getOrDefault("8",capacity)).intValue();
        this.roundDuration = ((Number)properties.getOrDefault("2",roundDuration)).longValue();
        this.overtime = ((Number)properties.getOrDefault("3",overtime)).longValue();
        this.name = (String)properties.get("5");
        this.timestamp = ((Number)properties.getOrDefault("6",0)).longValue();
        this.levelLimit = ((Number)properties.getOrDefault("7",levelLimit)).intValue();
        this.index = (String)properties.get("9");
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
    public void update() {
        arenas.forEach((a)->{
            if(!this.dataStore.update(a)){//failed if no key associated
                this.dataStore.create(a);
            }
        });
        this.timestamp = TimeUtil.toUTCMilliseconds(LocalDateTime.now());
        this.dataStore.update(this);
    }
    public void start(){
        //always to start max match queue to avoid level limit refresh
        if(levelLimit==0){//assign default limit from descriptor capacity
            levelLimit = this.descriptor.capacity();
        }
        listArena();
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
    @Override
    public void registerListener(Listener listener){
        this.listener = listener;
    }
    public void onConfiguration(Consumable consumable){
        aMap.forEach((k,a)->{
            if(a.name().equals(consumable.configurationName())){
                a.consumable = consumable;
            }
        });
    }
}
