package com.tarantula.platform.item;

import com.icodesoftware.RecoverableFactory;
import com.tarantula.platform.presence.PresencePortableRegistry;

/**
 * Created by yinghu on 1/21/2021.
 */
public class ItemPackQuery implements RecoverableFactory<ItemPack> {

    private String owner;

    public ItemPackQuery(String owner){
        this.owner = owner;
    }

    public ItemPack create() {
        return new ItemPack();
    }

    public String distributionKey() {
        return owner;
    }

    public  int registryId(){
        return PresencePortableRegistry.ITEM_PACK_CID;
    }

    public String label(){
        return "ItemPack";
    }
}
