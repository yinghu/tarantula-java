package com.tarantula.platform.item;

import com.icodesoftware.RecoverableFactory;

public class ItemQuery implements RecoverableFactory<Item> {

    public String label;

    public ItemQuery(){}

    public ItemQuery(String configurationType){
        this.label = configurationType;
    }

    @Override
    public Item create() {
        return new Item();
    }

    @Override
    public int registryId() {
        return ItemPortableRegistry.ITEM_CID;
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public String distributionKey() {
        return null;
    }
}
