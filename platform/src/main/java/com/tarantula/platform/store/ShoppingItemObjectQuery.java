package com.tarantula.platform.store;

import com.icodesoftware.RecoverableFactory;
import com.tarantula.platform.item.ItemPortableRegistry;

public class ShoppingItemObjectQuery implements RecoverableFactory<ShoppingItem> {

    public String label;


    public ShoppingItemObjectQuery(String query){
        this.label = query;
    }

    @Override
    public ShoppingItem create() {
        return new ShoppingItem();
    }

    @Override
    public int registryId() {
        return ItemPortableRegistry.SHOPPING_ITEM_CID;
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
