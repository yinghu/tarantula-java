package com.tarantula.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.icodesoftware.Connection;

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
    static final int CLOSING = 4;
    static final int OVERTIME = 5; //waiting for ending
    static final int ENDING = 6; //ending game
    static final int TIMEOUT = 7; //end without connection
    static final int PENDING_END = 8;

    static final long PENDING_TIME = 5000;//5 SECONDS
    static final long TIMER_DELTA = 1000; //1 SECOND

    public static final int INTEGRATED_MODE = 1;
    public static final int OFF_LINE_MODE = 0;

    private int capacity;
    private int joinsOnStart;
    private int totalJoined;
    private boolean online;
    private int  rankUpBase;
    private Arena arena;
    private Connection connection;
    private long initialTime;
    private long duration;
    private long closingDuration;
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
        if(online&&(connection==null||connection.disabled())){
            this.roomListener.onTimeout(this);
            return null;
        }
        if(state>=CLOSING){
            return null;
        }
        totalJoined++;
        Stub _stub = pQueue.poll();
        _stub.rating = rating;
        _stub.totalJoined = totalJoined;
        if(totalJoined==joinsOnStart){
            state = INITIALIZING;
            initialTime = PENDING_TIME;
        }
        else if(state==WAITING){
            state = PENDING_JOIN;
            initialTime = PENDING_TIME*6;
        }
        if(totalJoined<capacity){
            roomListener.onJoining(this);
        }
        return _stub;
    }
    public synchronized boolean rejoin(Stub stub){
        stub.started = state>=STARTING;
        stub.totalJoined = totalJoined;
        return connection!=null&&(!connection.disabled());
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
    public void reset(int capacity,int joinsOnStart,long duration,boolean online,int rankUpBase,Arena arena){
        this.capacity = capacity;
        this.joinsOnStart = joinsOnStart;
        this.duration = duration;
        this.closingDuration = 60000;
        this.online = online;
        this.arena = arena;
        this.rankUpBase = rankUpBase;
        this.pQueue = new ArrayDeque<>(this.capacity);
        this.stubs = new Stub[this.capacity];
        for(int i=0;i<this.capacity;i++){
            Stub stub = new Stub(i,roomId);
            stub.capacity = this.capacity;
            stub.arena = this.arena.name();
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
    public void reset(boolean connectionRemoved){
        totalJoined = 0;
        state = WAITING;
        this.stubs = new Stub[this.capacity];
        for(int i=0;i<this.capacity;i++){
            Stub stub = new Stub(i,roomId);
            stub.capacity = this.capacity;
            this.pQueue.offer(stub);
            this.stubs[i] = stub;
        }
        connection.disabled(connectionRemoved);
    }
    public int round(){
        return round;
    }
    public Stub[] playerList(){
        return this.stubs;
    }
    public int totalJoined(){return this.totalJoined;}
    public boolean offline(){
        return !this.online;
    }
    public int capacity(){
        return this.capacity;
    }
    public int joinsOnStart(){
        return joinsOnStart;
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
                if(duration<=0){//goes to closing time
                    state = CLOSING;
                    pendingUpdate = this.roomListener.onClosing(this);
                }
                break;
            case CLOSING:
                closingDuration -= TIMER_DELTA;
                if(closingDuration<=0){
                    state = OVERTIME;
                    overtime = PENDING_TIME;
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
    public void onUpdated(String action,byte[] payload){
        //System.out.println(action+">"+new String(payload));
        JsonObject jsonObject = new JsonParser().parse(new String(payload)).getAsJsonObject();
        if(action.equals("onStats")){
            JsonArray stats = jsonObject.getAsJsonArray("stats");
            stats.forEach((j)->{
                JsonObject js = j.getAsJsonObject();
                int seat = js.get("seat").getAsInt();
                Stub stub = stubs[seat];
                roomListener.onStatistics(stub.owner(),js.get("category").getAsString(),js.get("delta").getAsDouble());
            });
        }
        else if(action.equals("onClose")){
            JsonArray ratings = jsonObject.getAsJsonArray("ratings");
            ratings.forEach((j)->{
                JsonObject js = j.getAsJsonObject();
                int seat = js.get("seat").getAsInt();
                Stub stub = stubs[seat];
                stub.rank = js.get("rank").getAsInt();
                stub.pxp = js.get("xp").getAsDouble();
                roomListener.onRating(stub,rankUpBase);
            });
        }
    }
}
