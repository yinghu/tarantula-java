package com.tarantula.platform.item;

import com.icodesoftware.RecoverableFactory;

public class ItemQuery implements RecoverableFactory<Item> {
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
        return null;
    }

    @Override
    public String distributionKey() {
        return null;
    }
}
