package com.tarantula.platform.inventory;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.SnowflakeKey;

public class InventoryItemQuery implements RecoverableFactory<InventoryItem> {

    private long inventoryId;


    public InventoryItemQuery(long inventoryId){
        this.inventoryId = inventoryId;
    }

    public InventoryItem create() {
        return new InventoryItem();
    }


    public String label(){
        return InventoryItem.LABEL;
    }

    @Override
    public Recoverable.Key key() {
        return new SnowflakeKey(inventoryId);
    }
}
