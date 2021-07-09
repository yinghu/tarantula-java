package com.tarantula.game;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.AssociateKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DynamicZone extends RecoverableObject implements GameZone {

    protected String playMode;
    protected int levelLimit;
    protected int capacity;

    protected List<Arena> arenaList;
    protected ConcurrentHashMap<Integer,Arena> levelList;

    public DynamicZone(){
        this.label = "Zone";
        this.arenaList = new ArrayList<>();
        this.levelList = new ConcurrentHashMap<>();
    }
    
    public DynamicZone(String name,String playMode,int capacity,int levelLimit){
        this();
        this.name = name;
        this.playMode = playMode;
        this.capacity = capacity;
        this.levelLimit = levelLimit;
    }

    public Stub join(Rating rating){
        return null;
    }
    public void addArena(Arena arena){

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
        //this.properties.put("2",roundDuration);
        //this.properties.put("3",overtime);
        this.properties.put("5",name);
        this.properties.put("6",this.timestamp);
        this.properties.put("7",this.levelLimit);
        //this.properties.put("8",this.joinsOnStart);
        this.properties.put("9",this.index);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.capacity = ((Number)properties.getOrDefault("1",capacity)).intValue();
        //this.joinsOnStart = ((Number)properties.getOrDefault("8",capacity)).intValue();
        //this.roundDuration = ((Number)properties.getOrDefault("2",roundDuration)).longValue();
        //this.overtime = ((Number)properties.getOrDefault("3",overtime)).longValue();
        this.name = (String)properties.get("5");
        this.timestamp = ((Number)properties.getOrDefault("6",0)).longValue();
        this.levelLimit = ((Number)properties.getOrDefault("7",levelLimit)).intValue();
        this.index = (String)properties.get("9");
    }

}
