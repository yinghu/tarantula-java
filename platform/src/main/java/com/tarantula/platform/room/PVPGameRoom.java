package com.tarantula.platform.room;

import com.hazelcast.nio.serialization.Portable;
import com.tarantula.platform.event.PortableEventRegistry;

import java.util.HashMap;


public class PVPGameRoom extends GameRoomHeader implements Portable {

    public PVPGameRoom(int capacity){
       super(capacity);
       this.joinIndex = new HashMap<>(capacity);
    }
    public PVPGameRoom(){
       super(0);
    }

    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableEventRegistry.PVP_ROOM_CID;
    }


    protected GameRoom duplicate(){
        PVPGameRoom _room = new PVPGameRoom();
        _room.entries = new GameEntry[this.capacity];
        joinIndex.forEach((k,e)->_room.entries[e.seat()]=e);
        _room.capacity = this.capacity;
        _room.round = this.round;
        return _room;
    }
}