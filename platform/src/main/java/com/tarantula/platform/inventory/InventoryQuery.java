package com.tarantula.platform.inventory;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.SnowflakeKey;

public class InventoryQuery implements RecoverableFactory<Inventory> {

    private long systemId;


    public InventoryQuery(long systemId){
        this.systemId = systemId;
    }

    public Inventory create() {
        return new Inventory();
    }


    public String label(){
        return Inventory.LABEL;
    }

    @Override
    public Recoverable.Key key() {
        return new SnowflakeKey(systemId);
    }
}
