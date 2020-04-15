package com.tarantula.game;

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
    static final int BATTLING = 3; //battling
    static final int OVERTIME = 4; //waiting for ending
    static final int ENDING = 5; //ending game
    static final int PENDING_END = 6;

    static final long PENDING_TIME = 5000;//5 SECONDS
    static final long TIMER_DELTA = 1000; //1 SECOND
    private int capacity;
    private int totalJoined;
    private boolean dedicated;

    private long timerDelta;
    private long initialTime;
    private long duration;
    private long overtime;

    private int round;
    private int state;

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
        return pQueue.poll();
    }
    public synchronized void leave(Stub stub){
        totalJoined--;
        
        pQueue.offer(stub);
        roomListener.onWaiting(this);
    }

    public void start(int capacity,long duration,boolean dedicated,RoomListener roomListener){
        this.capacity = capacity;
        this.duration = duration;
        this.dedicated = dedicated;
        this.timerDelta = TIMER_DELTA;
        this.initialTime = PENDING_TIME;
        this.overtime = PENDING_TIME;
        this.round++;
        this.totalJoined=0;
        this.pQueue = new ArrayDeque<>(this.capacity);
        for(int i=0;i<this.capacity;i++){
            this.pQueue.offer(new Stub(i,this.oid));
        }
        this.state = WAITING;
        this.roomListener = roomListener;
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
                initialTime-=timerDelta;
                if(initialTime<=0){
                    initialTime = 5*1000;
                }
                update.on(oid+"?onTimer",new Countdown(initialTime).toJson().toString().getBytes());
                break;
            case INITIALIZING:

                break;
            case BATTLING:
                break;
            case OVERTIME:
                break;
            case ENDING:
                break;
        }
        /**
        if(totalJoined==0){
            return;
        }
        duration = duration-1000;
        if(duration<=0){
            update.on(oid+"?onLeave",null);
            totalJoined=0;
            return;
        }
        update.on(oid+"?onTimer",new Countdown(duration).toJson().toString().getBytes());
         **/
    }
}
