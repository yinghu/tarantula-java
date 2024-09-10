package com.tarantula.platform.lobby;

import com.google.gson.JsonObject;
import com.tarantula.platform.item.Component;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class RoomItem extends Component {

    public RoomItem(){

    }

    public RoomItem(JsonObject payload){
        this.header = payload;
    }
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.ROOM_ITEM_CID;
    }

    public int capacity(){
        return header.get("Capacity").getAsInt();
    }
    public int joinsOnStart(){
        return header.get("JoinsOnStart").getAsInt();
    }
    public long duration(){
        return header.get("Duration").getAsInt()*1000*60;
    }
    public long overtime(){
        return header.get("Overtime").getAsInt()*1000*60;
    }
}
