package com.icodesoftware.integration.udp;

import com.icodesoftware.Room;
import com.icodesoftware.protocol.GameModule;
import com.icodesoftware.util.RecoverableObject;

import java.util.concurrent.atomic.AtomicInteger;

public class ActiveRoom extends RecoverableObject implements Room {

    private int capacity;
    private long duration;
    private long overtime;
    private int joinsOnStart;
    private int timeout;

    private int channelId;

    private AtomicInteger totalJoined;
    private AtomicInteger totalLeft;

    public GameModule gameModule;
    public GameUserChannel gameUserChannel;

    private long countdownTimer;

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
        this.totalJoined = new AtomicInteger(0);
        this.totalLeft = new AtomicInteger(0);
        this.countdownTimer = this.duration + this.overtime;
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
        return totalJoined.get() < capacity;
    }

    public int totalJoined(){
        return totalJoined.get();
    }

    public int totalLeft(){
        return totalLeft.get();
    }

    public void onCountdown(long delta){
        countdownTimer -= delta;
        this.gameModule.countdown(countdownTimer);
    }

    public int join(){
        return totalJoined.incrementAndGet();
    }
    public int leave(){
        return totalLeft.incrementAndGet();
    }
    public ActiveRoom assign(int channelId){
        return new ActiveRoom(channelId,capacity,duration,overtime,joinsOnStart,timeout);
    }
}
