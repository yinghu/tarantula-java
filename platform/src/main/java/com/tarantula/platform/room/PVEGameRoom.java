package com.tarantula.platform.room;

import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.tarantula.platform.event.PortableEventRegistry;

public class PVEGameRoom extends GameRoomHeader implements Portable {

    public PVEGameRoom(){
        this.capacity = 1;
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
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("capacity",capacity);
        jsonObject.addProperty("duration",duration);
        jsonObject.addProperty("round",round);
        return jsonObject;
    }

    public synchronized PVEGameRoom join(String systemId,RoomListener roomListener){
        return this;
    }
    public synchronized boolean leave(String systemId){
        return true;
    }
    public synchronized GameRoom view(){
        return this;
    }
}