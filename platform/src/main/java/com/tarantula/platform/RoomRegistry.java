package com.tarantula.platform;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.RecoverableObject;


import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RoomRegistry extends RecoverableObject {

    public static int JOINED = 0;
    public static int FULLY_JOINED = 1;
    public static int ALREADY_JOINED = 2;
    public static int NOT_JOINED = 3;

    protected Set<String> players = new HashSet<>();
    protected int maxSize;
    protected int totalJoined;

    public RoomRegistry(){
        this.label = "register";
        this.totalJoined = 0;
    }
    public RoomRegistry(int maxSize){
        this();
        this.maxSize = maxSize;
    }
    public int addPlayer(String systemId){
        synchronized (players){
            if(players.contains(systemId)){
                return ALREADY_JOINED;
            }
            if(totalJoined+1>maxSize){
                return NOT_JOINED;
            }
            totalJoined++;
            players.add(systemId);
            return totalJoined==maxSize?FULLY_JOINED:JOINED;
        }
    }
    public boolean removePlayer(String systemId){
        synchronized (players){
            players.remove(systemId);
            totalJoined--;
            return totalJoined==0;
        }
    }
    public boolean fullJoined(){
        synchronized (players){
            return totalJoined!=0&&totalJoined==maxSize;
        }
    }

    public boolean empty(){
        synchronized (players){
            return totalJoined==0;
        }
    }
    @Override
    public Map<String,Object> toMap(){
        players.forEach((k)->{
            properties.put(k,"1");
        });
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        properties.forEach((k,v)->{
            players.add(k);
            totalJoined++;
        });
    }
    @Override
    public Key key(){
        return new AssociateKey(this.bucket,this.oid,this.label);
    }
    @Override
    public String distributionKey() {
        if(this.bucket==null || this.oid==null){
            return null;
        }
        return new StringBuffer(this.bucket).append(Recoverable.PATH_SEPARATOR).append(oid).append(Recoverable.PATH_SEPARATOR).append(label).toString();
    }
    @Override
    public void distributionKey(String distributionKey) {
        String[] klist = distributionKey.split(Recoverable.PATH_SEPARATOR);
        this.bucket = klist[0];
        this.oid = klist[1];
    }
    public String instanceId(){
        return new StringBuffer(this.bucket).append(Recoverable.PATH_SEPARATOR).append(oid).toString();
    }
}
