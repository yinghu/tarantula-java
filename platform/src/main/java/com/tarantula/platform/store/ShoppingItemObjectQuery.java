package com.tarantula.platform.store;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.tarantula.platform.item.ItemPortableRegistry;

public class ShoppingItemObjectQuery implements RecoverableFactory<Shop> {

    public String label;
    private Recoverable.Key key;

    public ShoppingItemObjectQuery(Recoverable.Key key,String query){
        this.key = key;
        this.label = query;
    }

    @Override
    public Shop create() {
        return new Shop();
    }

    @Override
    public int registryId() {
        return ItemPortableRegistry.SHOP_CID;
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public Recoverable.Key key() {
        return key;
    }
}
