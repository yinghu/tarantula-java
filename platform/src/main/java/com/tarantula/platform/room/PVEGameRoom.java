package com.tarantula.platform.room;

import com.tarantula.platform.event.PortableEventRegistry;

public class PVEGameRoom extends GameRoomHeader{

    public PVEGameRoom(){
        super(1);
    }

    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PortableEventRegistry.PVE_ROOM_CID;
    }

    protected GameRoom duplicate() {
        PVEGameRoom _room = new PVEGameRoom();
        _room.capacity = this.capacity;
        _room.round = this.round;
        _room.dedicated = this.dedicated;
        _room.duration = this.duration;
        _room.joinsOnStart = this.joinsOnStart;
        _room.overtime = this.overtime;
        if (!this.dedicated){
            _room.entries = new GameEntry[this.capacity];
            joinIndex.forEach((k, e) -> _room.entries[e.seat()] = e);
        }
        _room.arena = arena;
        return _room;
    }
}