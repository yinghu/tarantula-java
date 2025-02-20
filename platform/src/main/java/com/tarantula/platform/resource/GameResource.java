package com.tarantula.platform.resource;

import com.icodesoftware.protocol.ApplicationResource;
import com.tarantula.platform.item.Application;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.item.Item;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.util.ArrayList;
import java.util.List;

public class GameResource extends Application implements ApplicationResource {

    public GameResource(){

    }

    public GameResource(ConfigurableObject configurableObject){
        super(configurableObject);
    }

    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.GAME_RESOURCE_CID;
    }

    @Override
    public String name(){
        if(header.has("Name")) return header.get("Name").getAsString();
        return configurationName;
    }

    public List<Item> list(){
        ArrayList<Item> items = new ArrayList<>();
        _reference.forEach(c->{
            new Item(c).list().forEach(x->{
                items.add(new Item(x));//sku item
            });
        });
        return items;
    }

}
