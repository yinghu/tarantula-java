package com.tarantula.platform.lobby;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;
import com.tarantula.platform.item.Application;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.util.ArrayList;
import java.util.List;

public class LobbyItem extends Application{

    public final int publishId;
    public LobbyItem(){
        this.publishId = 0;
    }

    public LobbyItem(JsonObject payload){
        this.publishId = payload.get("ConfigurationPublishId").getAsInt();
        this.header = payload;
        this.configurationName = payload.get("ConfigurationName").getAsString();
    }

    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.LOBBY_ITEM_CID;
    }


    public String name(){
        return this.header.get("Name").getAsString();
    }

    public List<ZoneItem> zoneList(){
        ArrayList<ZoneItem> zlist = new ArrayList<>();
        if(!header.has("_zoneList")){
            _reference.forEach(ref->{
                zlist.add((ZoneItem)ref);
            });
            return zlist;
        }
        header.get("_zoneList").getAsJsonArray().forEach(z->{
            JsonObject zo = z.getAsJsonObject();
            ZoneItem zoneItem = new ZoneItem(zo);
            zlist.add(zoneItem);
        });
        return zlist;
    }

    @Override
    public  <T extends Configurable> T setup(){
        _reference = new ArrayList<>();
        for(JsonElement je : reference){
            ZoneItem cob = new ZoneItem();
            cob.distributionKey(je.getAsString());
            cob.dataStore(dataStore);
            if(this.dataStore.load(cob) && !cob.configurationType().equals(Configurable.APPLICATION_CONFIG_TYPE)){
                cob.registerListener(this);
                _reference.add(cob.setup());
            }
            else{
                validated = false;
                break;
            }
        }
        return (T)this;
    }


}
