package com.tarantula.platform.inventory;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.SnowflakeKey;

public class InventoryQuery implements RecoverableFactory<UserInventory> {

    private long systemId;
    private String label;

    public InventoryQuery(long systemId){
        this.systemId = systemId;
    }
    public InventoryQuery(long systemId,String label){
        this.systemId = systemId;
        this.label = label;
    }

    public UserInventory create() {
        return new UserInventory();
    }


    public String label(){
        return label==null?UserInventory.LABEL:label;
    }

    @Override
    public Recoverable.Key key() {
        return new SnowflakeKey(systemId);
    }
}
