package com.tarantula.game;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tarantula.Connection;
import com.tarantula.Module;
import com.tarantula.platform.statistics.StatsDelta;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.UUID;

/**
 * Updated by yinghu lu on 6/11/2020.
 */
public class Room implements Connection.StateListener{

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
    static final int CONNECTION_RETRIES = 3; //1 SECOND

    public static final int DEDICATED_MODE = 2;
    public static final int INTEGRATED_MODE = 1;
    public static final int OFF_LINE_MODE = 0;

    private int capacity;
    private int totalJoined;
    private boolean online;
    private int level;
    private Connection connection;
    private int retries;
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
        totalJoined++;
        Stub _stub = pQueue.poll();
        _stub.rating = rating;
        if(totalJoined==capacity){
            state = INITIALIZING;
        }
        else{
            roomListener.onWaiting(this);
            state =  PENDING_JOIN;
        }
        initialTime = PENDING_TIME;
        return _stub;
    }
    public synchronized boolean leave(Stub stub){
        if(state>PENDING_JOIN){
            return false;
        }
        roomListener.onLeaving(stub);
        totalJoined--;
        state = totalJoined>0?PENDING_JOIN:WAITING;
        pQueue.offer(stub);
        if(state==PENDING_JOIN&&stub.rating.xpLevel==this.level){
            //reset the lowest level
            stub.disabled(true);
            int mlevel = 10;
            for(Stub s : stubs) {
                if(!s.disabled()&&s.rating.xpLevel<mlevel){
                    mlevel = s.rating.xpLevel;
                }
            }
            level = mlevel;//requeue on lowest level
        }
        roomListener.onWaiting(this);
        return true;
    }
    private synchronized boolean end(){
        if(state==STARTING||state==OVERTIME){
            state = ENDING;
        }
        return true;
    }
    public void reset(int capacity,long duration,boolean online,int level){
        this.capacity = capacity;
        this.duration = duration;
        this.online = online;
        this.level = level;
        this.pQueue = new ArrayDeque<>(this.capacity);
        this.stubs = new Stub[this.capacity];
        for(int i=0;i<this.capacity;i++){
            Stub stub = new Stub(i,roomId);
            this.pQueue.offer(stub);
            this.stubs[i] = stub;
        }
    }
    public void start(RoomListener roomListener){
        this.initialTime = PENDING_TIME;
        this.overtime = PENDING_TIME;
        this.round++;
        this.retries = CONNECTION_RETRIES;
        this.totalJoined=0;
        this.state = WAITING;
        this.connection = null;
        this.roomListener = roomListener;
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
    public int level(){
        return this.level;
    }
    public Connection connection(){
        return this.connection;
    }


    public synchronized void onTimer(Module.OnUpdate update){
        switch (state){
            case WAITING:
                break;
            case PENDING_JOIN:
                initialTime -=TIMER_DELTA;
                if(initialTime<=0){
                    initialTime = PENDING_TIME;
                }
                update.on("",roomId+"?onTimer",new Countdown(initialTime,state,totalJoined).toJson().toString().getBytes());
                break;
            case INITIALIZING:
                initialTime -=TIMER_DELTA;
                if(initialTime>=0){
                    update.on("",roomId+"?onTimer",new Countdown(initialTime,state,totalJoined).toJson().toString().getBytes());
                    if(online&&this.connection==null){//fetch connection per timer loop
                        this.connection = this.roomListener.onConnection(this);
                        if(this.connection!=null){
                            this.roomListener.onConnecting(this);
                        }
                    }
                }
                else{
                    if(!online){//offline mode
                        state = STARTING;
                        update.on("",roomId+"?onStart",this.roomListener.onStarting(this));
                    }else{
                        if(this.connection!=null){//go to
                            state = STARTING;
                            update.on("",roomId+"?onStart",this.roomListener.onStarting(this));
                        }
                        else{
                            retries--;
                            if(retries<=0){
                                state = TIMEOUT; //timeout without available connection
                            }else{
                                initialTime = PENDING_TIME;
                            }
                        }
                    }
                }
                break;
            case STARTING:
                duration -=TIMER_DELTA;
                if(duration<=0){//goes to overtime
                    state = OVERTIME;
                    update.on("",roomId+"?onOvertime",new Countdown(overtime,state,totalJoined).toJson().toString().getBytes());
                }
                //else if(!dedicated){
                    //update.on(oid+"?onTimer",new Countdown(duration,state,totalJoined).toJson().toString().getBytes());
                //}
                break;
            case OVERTIME:
                overtime -=TIMER_DELTA;
                if(overtime<=0){
                    state = ENDING;
                }
                //else if(!dedicated){
                    //update.on(oid+"?onTimer",new Countdown(overtime,state,totalJoined).toJson().toString().getBytes());
                //}
                break;
            case ENDING:
                update.on("",roomId+"?onEnd",new Countdown(overtime,state,totalJoined).toJson().toString().getBytes());
                state = PENDING_END;
                initialTime = PENDING_TIME;
                break;
            case TIMEOUT:
                state = WAITING;
                update.on("",roomId+"?onEnd",new Countdown(overtime,state,totalJoined).toJson().toString().getBytes());
                roomListener.onTimeout(this);
                break;
            case PENDING_END:
                initialTime -=TIMER_DELTA;
                if(initialTime<=0){//delay 5 seconds to wait for game server result
                    roomListener.onEnding(this);
                }
        }
    }

    @Override
    public void onUpdated(byte[] updated) {
        JsonParser jp = new JsonParser();
        InputStreamReader inr = new InputStreamReader(new ByteArrayInputStream(updated));
        JsonObject j = jp.parse(inr).getAsJsonObject();
        j.entrySet().forEach((e)->{
            if(e.getKey().equals("gains")){
                e.getValue().getAsJsonArray().forEach((st)->{
                    JsonObject jo = st.getAsJsonObject();
                    Stub stub = stubs[jo.get("seat").getAsInt()];
                    stub.stats = new StatsDelta(jo.get("name").getAsString(),jo.get("value").getAsDouble());
                    this.roomListener.onUpdating(stub);
                });
            }
        });
    }

    @Override
    public void onEnded(byte[] ended) {
        this.end();
        JsonParser jp = new JsonParser();
        InputStreamReader inr = new InputStreamReader(new ByteArrayInputStream(ended));
        JsonObject j = jp.parse(inr).getAsJsonObject();
        j.entrySet().forEach((e)->{
            if(e.getKey().equals("gains")){
                e.getValue().getAsJsonArray().forEach((st)->{
                    JsonObject jo = st.getAsJsonObject();
                    Stub stub = stubs[jo.get("seat").getAsInt()];
                    stub.stats = new StatsDelta(jo.get("name").getAsString(),jo.get("value").getAsDouble());
                    this.roomListener.onUpdating(stub);
                });
            }
            if(e.getKey().equals("ratings")){
                e.getValue().getAsJsonArray().forEach((st)->{
                    JsonObject jo = st.getAsJsonObject();
                    Stub stub = stubs[jo.get("seat").getAsInt()];
                    stub.rank = jo.get("rank").getAsInt();
                    stub.pxp = jo.get("xp").getAsDouble();
                });
            }
        });
    }
}
