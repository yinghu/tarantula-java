package com.tarantula.platform.room;
import com.hazelcast.nio.serialization.Portable;

import com.tarantula.platform.event.PortableEventRegistry;

import java.util.HashMap;
import java.util.Map;

public class TVEGameRoom extends GameRoomHeader implements Portable {


    private HashMap<String,GameEntry> joinIndex;
    private GameEntry[] entries;

    public TVEGameRoom(int capacity){
        super(capacity);
        joinIndex = new HashMap<>(capacity);
    }
    public TVEGameRoom(){
        super(0);
    }


    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",capacity);
        this.properties.put("2",round);
        this.properties.put("3",this.index);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.capacity = ((Number)properties.getOrDefault("1",12)).intValue();
        this.round = ((Number)properties.getOrDefault("2",0)).intValue();
        this.index = (String)properties.getOrDefault("3","");
    }
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PortableEventRegistry.TVE_ROOM_CID;
    }




    protected TVEGameRoom duplicate(){
        TVEGameRoom _room = new TVEGameRoom();
        _room.entries = new GameEntry[joinIndex.size()];
        joinIndex.forEach((k,e)->_room.entries[e.seat()]=e);
        _room.capacity = _room.entries.length;
        _room.round = this.round;
        _room.bucket(this.bucket);
        _room.oid(this.oid);
        return this;
    }
}