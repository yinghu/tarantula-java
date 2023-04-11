package com.icodesoftware.integration.udp;

import com.icodesoftware.Room;
import com.icodesoftware.util.RecoverableObject;

public class ActiveRoom extends RecoverableObject implements Room {

    private int capacity;
    private long duration;
    private long overtime;
    private int joinsOnStart;
    private int timeout;

    private int channelId;

    public ActiveRoom(int capacity, long duration, long overtime, int joinsOnStart, int timeout){
        this.capacity = capacity;
        this.duration = duration;
        this.overtime = overtime;
        this.joinsOnStart = joinsOnStart;
        this.timeout = timeout;
    }

    public ActiveRoom(int channelId,int capacity, long duration, long overtime, int joinsOnStart, int timeout){
        this.channelId = channelId;
        this.capacity = capacity;
        this.duration = duration;
        this.overtime = overtime;
        this.joinsOnStart = joinsOnStart;
        this.timeout = timeout;
    }

    public int channelId(){
        return this.channelId;
    }

    @Override
    public int capacity() {
        return capacity;
    }

    @Override
    public long duration() {
        return duration;
    }

    @Override
    public long overtime() {
        return overtime;
    }

    @Override
    public int round() {
        return 0;
    }

    @Override
    public int timeout() {
        return timeout;
    }

    @Override
    public int joinsOnStart() {
        return joinsOnStart;
    }

    @Override
    public boolean dedicated() {
        return true;
    }

    @Override
    public   boolean available(){
        return true;
    }

    public int totalJoined(){
        return 0;
    }

    public int totalLeft(){
        return 0;
    }

    public ActiveRoom assign(int channelId){
        return new ActiveRoom(channelId,capacity,duration,overtime,joinsOnStart,timeout);
    }
}
