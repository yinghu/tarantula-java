package com.tarantula.platform.room;

import com.tarantula.platform.event.PortableEventRegistry;


public class TVEGameRoom extends GameRoomHeader{




    public TVEGameRoom(int capacity){
        super(capacity);
    }
    public TVEGameRoom(){
        super(0);
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
        return this;
    }
}