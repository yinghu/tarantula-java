package com.tarantula.platform.room;


import com.tarantula.platform.event.PortableEventRegistry;


public class TVTGameRoom extends GameRoomHeader{



    public TVTGameRoom(int capacity){
        super(capacity);
    }
    public TVTGameRoom(){
        super(0);
    }


    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PortableEventRegistry.TVT_ROOM_CID;
    }




    protected TVTGameRoom duplicate(){
        TVTGameRoom _room = new TVTGameRoom();
        _room.entries = new GameEntry[joinIndex.size()];
        joinIndex.forEach((k,e)->_room.entries[e.seat()]=e);
        _room.capacity = _room.entries.length;
        _room.round = this.round;
        return this;
    }
}