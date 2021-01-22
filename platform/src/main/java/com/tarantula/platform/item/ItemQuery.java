package com.tarantula.platform.item;

import com.icodesoftware.RecoverableFactory;
import com.tarantula.platform.presence.PresencePortableRegistry;

/**
 * Created by yinghu on 1/21/2021.
 */
public class ItemQuery implements RecoverableFactory<Item> {

    private String owner;

    public ItemQuery(String owner){
        this.owner = owner;
    }

    public Item create() {
        return new Item();
    }

    public String distributionKey() {
        return owner;
    }

    public  int registryId(){
        return PresencePortableRegistry.ITEM_CID;
    }

    public String label(){
        return "Item";
    }
}
