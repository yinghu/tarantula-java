package com.tarantula.game;

import com.icodesoftware.Connection;
import com.icodesoftware.protocol.DataBuffer;
import com.icodesoftware.protocol.MessageHandler;

import java.util.ArrayDeque;
import java.util.UUID;

/**
 * Updated by yinghu lu on 6/11/2020.
 */
public class Room{

    static final int WAITING = 0; //waiting for first join
    static final int PENDING_JOIN = 1; //waiting after first join
    static final int INITIALIZING = 2; //starting game on full join
    static final int STARTING = 3; //battling
    static final int OVERTIME = 4; //waiting for ending
    static final int ENDING = 5; //ending game
    static final int TIMEOUT = 6; //end without connection
    static final int PENDING_END = 7;

    static final long PENDING_TIME = 5000;//5 SECONDS
    static final long TIMER_DELTA = 1000; //1 SECOND

    public static final int INTEGRATED_MODE = 1;
    public static final int OFF_LINE_MODE = 0;

    private int capacity;
    private int totalJoined;
    private boolean online;
    private int  rankUpBase;
    private Arena arena;
    private Connection connection;
    private long initialTime;
    private long duration;
    private long overtime;
    public String roomId;
    private int round;
    private int state;
    private Stub[] stubs;
    private ArrayDeque<Stub> pQueue;

    private RoomListener roomListener;

    public Room(){
        this.roomId = UUID.randomUUID().toString();
    }

    public synchronized Stub join(Rating rating){
        if(online&&connection==null){
            this.roomListener.onTimeout(this);
            return null;
        }
        totalJoined++;
        Stub _stub = pQueue.poll();
        _stub.rating = rating;
        if(totalJoined==capacity){
            state = INITIALIZING;
        }
        else{
            state =  PENDING_JOIN;
            roomListener.onJoining(this);
        }
        initialTime = PENDING_TIME*2;
        return _stub;
    }

    public synchronized boolean leave(Stub stub){
        if(state>PENDING_JOIN){
            return false;
        }
        totalJoined--;
        state = totalJoined>0?PENDING_JOIN:WAITING;
        pQueue.offer(stub);
        //NOTE : the room still keeps the initial level setting to play
        roomListener.onLeaving(this,stub);
        return true;
    }
    private synchronized boolean end(){
        if(state==STARTING||state==OVERTIME){
            state = ENDING;
        }
        return true;
    }
    public void reset(int capacity,long duration,boolean online,int rankUpBase,Arena arena){
        this.capacity = capacity;
        this.duration = duration;
        this.online = online;
        this.arena = arena;
        this.rankUpBase = rankUpBase;
        this.pQueue = new ArrayDeque<>(this.capacity);
        this.stubs = new Stub[this.capacity];
        for(int i=0;i<this.capacity;i++){
            Stub stub = new Stub(i,roomId);
            this.pQueue.offer(stub);
            this.stubs[i] = stub;
        }
        if(!online){
            return;
        }
        this.connection = roomListener.onConnecting(this);
    }
    public void start(RoomListener roomListener){
        this.initialTime = PENDING_TIME;
        this.overtime = PENDING_TIME;
        this.round++;
        this.totalJoined=0;
        this.state = WAITING;
        this.connection = null;
        if(pQueue!=null){
            pQueue.clear();
        }
        this.stubs = new Stub[0];
        if(roomListener!=null){
            this.roomListener = roomListener;
        }
    }
    public void reset(){
        totalJoined = 0;
        this.stubs = new Stub[this.capacity];
        for(int i=0;i<this.capacity;i++){
            Stub stub = new Stub(i,roomId);
            this.pQueue.offer(stub);
            this.stubs[i] = stub;
        }
    }
    public int round(){
        return round;
    }
    public Stub[] playerList(){
        return this.stubs;
    }
    public int state(){ return this.state;}
    public int totalJoined(){return this.totalJoined;}
    public boolean offline(){
        return !this.online;
    }
    public int capacity(){
        return this.capacity;
    }
    public int rankUpBase(){
        return this.rankUpBase;
    }
    public long duration(){
        return this.duration;
    }
    public long overtime(){ return this.overtime; }
    public Arena arena(){ return this.arena;}
    public Connection connection(){
        return this.connection;
    }
    public synchronized void connectionClosed(Connection closed){
        if(connection!=null&&closed.serverId().equals(connection.serverId())){
            connection.disabled(true);
        }
    }

    public synchronized PendingUpdate onTimer(){
        if(state==WAITING){
            return null;
        }
        PendingUpdate pendingUpdate = null;
        switch (state){
            case PENDING_JOIN:
                initialTime -=TIMER_DELTA;
                if(initialTime<=0){
                    state = TIMEOUT;
                }
                break;
            case INITIALIZING:
                initialTime -=TIMER_DELTA;
                if(initialTime<0){
                    state = STARTING;
                    pendingUpdate = this.roomListener.onStarting(this);
                }
                break;
            case STARTING:
                duration -=TIMER_DELTA;
                if(duration<=0){//goes to overtime
                    state = OVERTIME;
                    pendingUpdate = this.roomListener.onOverTiming(this);
                }
                break;
            case OVERTIME:
                overtime -=TIMER_DELTA;
                if(overtime<=0){
                    state = ENDING;
                    pendingUpdate = this.roomListener.onEnding(this);
                }
                break;
            case ENDING:
                state = PENDING_END;
                initialTime = PENDING_TIME;
                break;
            case TIMEOUT:
                state = WAITING;
                pendingUpdate = roomListener.onTimeout(this);
                break;
            case PENDING_END:
                initialTime -=TIMER_DELTA;
                if(initialTime<=0){//delay 5 seconds to wait for game server result
                    state = WAITING;
                    pendingUpdate = roomListener.onEnded(this);
                }
                break;
        }
        return pendingUpdate;
    }

    @Override
    public int hashCode(){
        return roomId.hashCode();
    }
    @Override
    public boolean equals(Object obj){
        return roomId.equals(((Room)obj).roomId);
    }
    public void onUpdated(byte[] payload){}
}
