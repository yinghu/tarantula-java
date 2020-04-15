package com.tarantula.game;

import com.tarantula.Module;
import com.tarantula.platform.RecoverableObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by yinghu lu on 4/14/2020.
 */
public class Arena extends RecoverableObject implements RoomListener {

    public int level =1;
    public double xp =100;
    public int capacity =2;
    public long roundDuration =60*1000;
    public boolean dedicated = false;
    public ConcurrentHashMap<String,Room> roomIndex;

    private CopyOnWriteArrayList<Room> rList = new CopyOnWriteArrayList<>();
    private ConcurrentLinkedDeque<Room> rQueue = new ConcurrentLinkedDeque<>();

    public Room room(){
        Room room = rQueue.poll();
        if(room==null){
            room = new Room();
            room.start(capacity,roundDuration,dedicated,this);
            rList.add(room);
            roomIndex.put(room.oid(),room);
        }
        return room;
    }

    public void start(){
        for(int i=0;i<3;i++){
            Room room = new Room();
            room.start(capacity,roundDuration,dedicated,this);
            rQueue.offer(room);
            rList.add(room);
            roomIndex.put(room.oid(),room);
        }
    }
    public void onTimer(Module.OnUpdate update){
        rList.forEach((r)->r.onTimer(update));
    }
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("level",level);
        this.properties.put("xp",xp);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.level =((Number)properties.get("level")).intValue();
        this.xp = ((Number)properties.get("xp")).doubleValue();
    }
    @Override
    public int getFactoryId() {
        return GamePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return GamePortableRegistry.ARENA_CID;
    }

    @Override
    public void onWaiting(Room room) {
        rQueue.addFirst(room);
    }
}
