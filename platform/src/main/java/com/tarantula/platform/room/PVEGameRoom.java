package com.tarantula.platform.room;

import com.hazelcast.nio.serialization.Portable;
import com.tarantula.platform.event.PortableEventRegistry;

public class PVEGameRoom extends GameRoomHeader implements Portable {

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



    @Override
    public synchronized GameRoom join(String systemId){
        return this;
    }
    @Override
    public synchronized void leave(String systemId){}



}