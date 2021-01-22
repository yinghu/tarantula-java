package com.tarantula.platform.item;

import com.tarantula.platform.presence.PresencePortableRegistry;

import java.util.List;
import java.util.Map;

public class ItemPack extends Item{

    public List<Item> itemList;

    public ItemPack(){
        this.onEdge = true;
        this.label = "ItemPack";
    }

    @Override
    public Map<String,Object> toMap(){
        super.toMap();
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        super.fromMap(properties);
    }

    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }


    public int getClassId() {
        return PresencePortableRegistry.ITEM_PACK_CID;
    }

}
