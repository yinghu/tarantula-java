package com.tarantula.game;

import com.icodesoftware.*;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.AssociateKey;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

abstract public class Zone extends RecoverableObject implements Configurable, DataStore.Updatable {

    public static final String PVE = "pve";
    public static final String PVP = "pvp";

    public ConcurrentHashMap<Integer,Arena> activeArenaIndex = new ConcurrentHashMap<>();
    public int levelLimit;
    public String mode;
    public Descriptor descriptor;
    public Zone(){
        this.label = "Zone";
    }
    public Zone(Descriptor descriptor){
        this();
        this.descriptor = descriptor;
    }

    abstract public Stub join(Rating rating);

    abstract public void update(ServiceContext serviceContext);

    @Override
    public Map<String,Object> toMap(){
        //this.properties.put("1",capacity);
        //this.properties.put("2",roundDuration);
        //this.properties.put("3",overtime);
        this.properties.put("4",mode);
        //this.properties.put("5",name);
        this.properties.put("6",this.timestamp);
        this.properties.put("7",this.levelLimit);
        //this.properties.put("8",this.joinsOnStart);
        this.properties.put("9",this.index);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        //this.capacity = ((Number)properties.getOrDefault("1",capacity)).intValue();
        //this.joinsOnStart = ((Number)properties.getOrDefault("8",capacity)).intValue();
        //this.roundDuration = ((Number)properties.getOrDefault("2",roundDuration)).longValue();
        //this.overtime = ((Number)properties.getOrDefault("3",overtime)).longValue();
        this.mode = (String)properties.getOrDefault("4",mode);
        //this.name = (String)properties.get("5");
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
}
