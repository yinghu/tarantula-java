package com.tarantula.platform.inventory;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.SnowflakeKey;

public class InventoryQuery implements RecoverableFactory<UserInventory> {

    private long systemId;


    public InventoryQuery(long systemId){
        this.systemId = systemId;
    }

    public UserInventory create() {
        return new UserInventory();
    }


    public String label(){
        return UserInventory.LABEL;
    }

    @Override
    public Recoverable.Key key() {
        return new SnowflakeKey(systemId);
    }
}
