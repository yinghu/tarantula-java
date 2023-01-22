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

}