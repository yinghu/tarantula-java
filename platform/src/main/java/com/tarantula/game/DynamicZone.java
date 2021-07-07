package com.tarantula.game;

import com.icodesoftware.util.RecoverableObject;

import java.util.ArrayList;
import java.util.List;
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


    public String playMode(){
        return playMode;
    }
    public int levelLimit(){
        return levelLimit;
    }
    public int capacity(){
        return capacity;
    }

}
