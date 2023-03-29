package com.icodesoftware.integration.udp;

import com.icodesoftware.Room;

public class DedicatedRoom implements Room {

    private int capacity;
    private long duration;
    private long overtime;
    private int joinsOnStart;
    private int timeout;

    public DedicatedRoom(int capacity,long duration,long overtime,int joinsOnStart,int timeout){
        this.capacity = capacity;
        this.duration = duration;
        this.overtime = overtime;
        this.joinsOnStart = joinsOnStart;
        this.timeout = timeout;
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
}
