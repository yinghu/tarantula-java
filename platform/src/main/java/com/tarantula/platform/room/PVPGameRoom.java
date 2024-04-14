package com.tarantula.platform.room;

import com.tarantula.platform.event.PortableEventRegistry;



public class PVPGameRoom extends GameRoomHeader{

    public PVPGameRoom(int capacity){
       super(capacity);
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


    protected GameRoom duplicate() {
        PVPGameRoom _room = new PVPGameRoom();
        _room.capacity = this.capacity;
        _room.dedicated = this.dedicated;
        _room.duration = this.duration;
        _room.joinsOnStart = this.joinsOnStart;
        _room.overtime = this.overtime;
        if (!this.dedicated){
            _room.entries = new GameEntry[this.capacity];
            joinIndex.forEach((k, e) -> _room.entries[e.seat()] = e);
        }
        return _room;
    }
}