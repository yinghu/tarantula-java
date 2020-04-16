package com.tarantula.game;

import com.google.gson.JsonObject;
import com.tarantula.Connection;
import com.tarantula.Descriptor;
import com.tarantula.Module;
import com.tarantula.platform.RecoverableObject;
import com.tarantula.platform.service.DeploymentServiceProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by yinghu lu on 4/14/2020.
 */
public class Arena extends RecoverableObject implements RoomListener {
    public String name ="Map";
    public int level =1;
    public double xp =100;
    public int capacity =1;
    public long roundDuration =60000;
    public long overtime = Room.PENDING_TIME;
    public boolean dedicated = true;
    public ConcurrentHashMap<String,Room> roomIndex;
    public ConcurrentHashMap<String,Stub> stubIndex;
    public DeploymentServiceProvider deploymentServiceProvider;
    public Descriptor descriptor;
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
    @Override
    public void onLeaving(Stub stub){
        stubIndex.remove(stub.owner());
    }
    @Override
    public Connection onConnection(Room room){
        return deploymentServiceProvider.onUDPConnection(descriptor.typeId(),(c)->{
            System.out.println(">>>>>>>>>>>>>>>>>>>"+room.oid());
        });
    }
    @Override
    public void onConnecting(Room room){
        this.deploymentServiceProvider.onStartedUDPConnection(room.connection().serverId(),roomSetting(room));
    }
    @Override
    public byte[] onStarting(Room room){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("arena",name);
        if(room.connection()!= null) {
            Connection connection = room.connection();
            JsonObject jcc = new JsonObject();
            jcc.addProperty("serverId", connection.serverId());
            jcc.addProperty("host",connection.host());
            jcc.addProperty("port",connection.port());
            jsonObject.add("connection", jcc);
        }
        return jsonObject.toString().getBytes();
    }
    @Override
    public void onEnding(Room room) {
        for(Stub sb : room.playerList()){
            stubIndex.remove(sb.owner());
        }
        room.start(capacity,roundDuration,dedicated,this);
        rQueue.addLast(room);
    }
    private byte[] roomSetting(Room room){
        JsonObject jo = new JsonObject();
        jo.addProperty("arena",name);
        jo.addProperty("duration",roundDuration/1000);
        jo.addProperty("overtime",overtime/1000);
        jo.addProperty("roomId",room.oid());
        jo.addProperty("key",room.playerList()[0].owner());
        return jo.toString().getBytes();
    }

}
