package com.tarantula.platform.lobby;

import com.google.gson.JsonArray;
import com.icodesoftware.Configurable;
import com.tarantula.platform.item.Item;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.util.ArrayList;
import java.util.List;

public class ZoneItem extends Item {

    private RoomItem room;

    public ZoneItem(){

    }

    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.ZONE_ITEM_CID;
    }

    public String playMode(){
        return header.get("PlayMode").getAsString();
    }
    public String name(){
        return header.get("Name").getAsString();
    }

    public RoomItem room(){

        return room;
    }

    public List<ArenaItem> arenaList(){
        ArrayList<ArenaItem> alist = new ArrayList<>();
        _reference.forEach(ref-> alist.add((ArenaItem) ref));
        return alist;
    }

    @Override
    public  <T extends Configurable> T setup(){
        room = new RoomItem();
        JsonArray roomId = application.get("Room").getAsJsonArray();
        room.distributionKey(roomId.get(0).getAsString());
        room.dataStore(dataStore);
        this.dataStore.load(room);
        room.setup();
        _reference = new ArrayList<>();
        JsonArray arenaId = application.get("ArenaSet").getAsJsonArray();
        arenaId.forEach(k->{
            ArenaItem arenaItem = new ArenaItem();
            arenaItem.distributionKey(k.getAsString());
            arenaItem.dataStore(dataStore);
            if(dataStore.load(arenaItem)) {
                arenaItem.setup();
                _reference.add(arenaItem);
            }

        });
        return (T)this;
    }





}
