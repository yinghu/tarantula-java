package com.tarantula.game;

import com.tarantula.Connection;
import com.tarantula.Module;
import com.tarantula.platform.RecoverableObject;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.UUID;
/**
 * Created by yinghu lu on 4/14/2020.
 */
public class Room extends RecoverableObject {

    static final int WAITING = 0; //waiting for first join
    static final int PENDING_JOIN = 1; //waiting after first join
    static final int INITIALIZING = 2; //starting game on full join
    static final int GAMING = 3; //battling
    static final int OVERTIME = 4; //waiting for ending
    static final int ENDING = 5; //ending game
    static final int PENDING_END = 6;

    static final long PENDING_TIME = 5000;//5 SECONDS
    static final long TIMER_DELTA = 1000; //1 SECOND
    static final int CONNECTION_RETRIES = 3; //1 SECOND
    private int capacity;
    private int totalJoined;
    private boolean dedicated;
    private Connection connection;
    private int retries;
    private long initialTime;
    private long duration;
    private long overtime;

    private int round;
    private int state;
    private Stub[] stubs;
    private ArrayDeque<Stub> pQueue;

    private RoomListener roomListener;

    public Room(){
        this.oid = UUID.randomUUID().toString();
    }
    public synchronized Stub join(){
        totalJoined++;
        if(totalJoined==capacity){
            state = INITIALIZING;
        }
        else{
            roomListener.onWaiting(this);
            state =  PENDING_JOIN;
        }
        initialTime = PENDING_TIME;
        return pQueue.poll();
    }
    public synchronized boolean leave(Stub stub){
        if(state>PENDING_JOIN){
            return false;
        }
        roomListener.onLeaving(stub);
        totalJoined--;
        state = totalJoined>0?PENDING_JOIN:WAITING;
        pQueue.offer(stub);
        roomListener.onWaiting(this);
        return true;
    }
    public void start(int capacity,long duration,boolean dedicated,RoomListener roomListener){
        this.capacity = capacity;
        this.duration = duration;
        this.dedicated = dedicated;
        this.initialTime = PENDING_TIME;
        this.overtime = PENDING_TIME;
        this.round++;
        this.retries = CONNECTION_RETRIES;
        this.totalJoined=0;
        this.pQueue = new ArrayDeque<>(this.capacity);
        this.stubs = new Stub[this.capacity];
        for(int i=0;i<this.capacity;i++){
            Stub stub = new Stub(i,oid);
            this.pQueue.offer(stub);
            this.stubs[i] = stub;
        }
        this.state = WAITING;
        this.connection = null;
        this.roomListener = roomListener;
    }
    public Stub[] playerList(){
        return this.stubs;
    }
    public Connection connection(){
        return this.connection;
    }
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("totalJoined",totalJoined);
        this.properties.put("round",this.round);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.totalJoined =((Number)properties.get("totalJoined")).intValue();
        this.round = ((Number)properties.get("round")).intValue();
    }
    @Override
    public int getFactoryId() {
        return GamePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return GamePortableRegistry.ROOM_CID;
    }
    public synchronized void onTimer(Module.OnUpdate update){
        switch (state){
            case PENDING_JOIN:
                initialTime -=TIMER_DELTA;
                if(initialTime<=0){
                    initialTime = PENDING_TIME;
                }
                update.on(oid+"?onTimer",new Countdown(initialTime,state).toJson().toString().getBytes());
                break;
            case INITIALIZING:
                initialTime -=TIMER_DELTA;
                if(initialTime>=0){
                    update.on(oid+"?onTimer",new Countdown(initialTime,state).toJson().toString().getBytes());
                    if(dedicated&&this.connection==null){//fetch connection per timer loop
                        this.connection = this.roomListener.onConnection(this);
                        if(this.connection!=null){
                            this.roomListener.onConnecting(this);
                        }
                    }
                }
                else{
                    if(!dedicated){//offline mode
                        state = GAMING;
                        update.on(oid+"?onStart",this.roomListener.onStarting(this));
                    }else{
                        if(this.connection!=null){//go to
                            state = GAMING;
                            update.on(oid+"?onStart",this.roomListener.onStarting(this));
                        }
                        else{
                            retries--;
                            if(retries<=0){
                                state = ENDING;
                            }else{
                                initialTime = PENDING_TIME;
                            }
                        }
                    }
                }
                break;
            case GAMING:
                duration -=TIMER_DELTA;
                if(duration<=0){
                    state = OVERTIME;
                }
                else{
                    update.on(oid+"?onGame",new Countdown(duration,state).toJson().toString().getBytes());
                }
                break;
            case OVERTIME:
                overtime -=TIMER_DELTA;
                if(overtime<=0){
                    state = ENDING;
                }
                else{
                    update.on(oid+"?onOvertime",new Countdown(overtime,state).toJson().toString().getBytes());
                }
                break;
            case ENDING:
                state = PENDING_END;
                update.on(oid+"?onEnd",null);
                roomListener.onEnding(this);
                break;
        }
    }
}
