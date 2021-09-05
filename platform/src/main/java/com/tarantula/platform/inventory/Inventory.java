package com.tarantula.platform.inventory;

import com.tarantula.platform.IndexSet;
import com.tarantula.platform.item.Commodity;

import java.util.ArrayList;
import java.util.List;

public class Inventory extends IndexSet {

    public Inventory(String category){
        this.label = "Inventory/"+category;
    }
    public void redeem(Commodity commodity){
        InventoryItem inventoryItem = new InventoryItem(commodity);
        dataStore.create(inventoryItem);
        keySet.add(inventoryItem.distributionKey());
        dataStore.update(this);
    }
    public List<InventoryItem> list(){
        return new ArrayList<>();
    }
}