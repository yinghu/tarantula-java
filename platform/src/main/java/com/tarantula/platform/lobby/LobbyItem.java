package com.tarantula.platform.lobby;

import com.google.gson.JsonElement;
import com.icodesoftware.Configurable;
import com.tarantula.platform.item.Application;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.util.ArrayList;
import java.util.List;

public class LobbyItem extends Application {

    public LobbyItem(){}

    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.LOBBY_ITEM_CID;
    }


    public String name(){
        return this.configurationName();
    }

    public List<ZoneItem> zoneList(){
        ArrayList<ZoneItem> zlist = new ArrayList<>();
        return zlist;
    }

    @Override
    public  <T extends Configurable> T setup(){
        _reference = new ArrayList<>();
        for(JsonElement je : reference){
            ConfigurableObject cob = new ConfigurableObject();
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
