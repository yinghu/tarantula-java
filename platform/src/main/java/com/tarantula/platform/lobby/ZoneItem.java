package com.tarantula.platform.lobby;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;
import com.tarantula.game.GameZone;
import com.tarantula.platform.item.Item;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.util.ArrayList;
import java.util.List;

public class ZoneItem extends Item {

    private RoomItem room;

    public ZoneItem(){

    }

    public ZoneItem(JsonObject payload){
        this.header = payload;
        this.configurationName = payload.get("ConfigurationName").getAsString();
        this.configurationVersion = payload.get("ConfigurationVersion").getAsString();
    }

    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.ZONE_ITEM_CID;
    }

    public String playMode(){
        int px = header.get("PlayMode").getAsInt();
        if(px==1) return GameZone.PLAY_MODE_PVP;
        if(px==2) return GameZone.PLAY_MODE_TVE;
        if(px==3) return GameZone.PLAY_MODE_TVT;
        return GameZone.PLAY_MODE_PVE;
    }
    public int rank(){
        return header.get("Rank").getAsInt();
    }
    public String name(){
        return header.get("Name").getAsString();
    }


    public RoomItem room(){
        RoomItem roomItem = new RoomItem(header.get("_room").getAsJsonObject());
        return roomItem;
    }

    public List<ArenaItem> arenaList(){
        ArrayList<ArenaItem> alist = new ArrayList<>();
        header.get("_arenaList").getAsJsonArray().forEach(a->{
            JsonObject ao = a.getAsJsonObject();
            ArenaItem arenaItem = new ArenaItem(ao);
            alist.add(arenaItem);
        });
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

    @Override
    public JsonObject toJson(){
        header.addProperty("ZoneId",this.distributionKey());
        return header;
    }




}
