package com.tarantula.platform.store;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;

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
    public String label() {
        return label;
    }

    @Override
    public Recoverable.Key key() {
        return key;
    }
}
