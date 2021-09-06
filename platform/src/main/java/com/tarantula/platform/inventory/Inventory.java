package com.tarantula.platform.inventory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.item.ItemPortableRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Inventory extends IndexSet implements Configurable {

    private ArrayList<InventoryItem> itemList = new ArrayList<>();

    public Inventory(){}

    public Inventory(String category){
        super("Inventory/"+category);
    }

    public void redeem(InventoryRedeemer commodity){
        InventoryItem inventoryItem = new InventoryItem(commodity);
        dataStore.create(inventoryItem);
        keySet.add(inventoryItem.distributionKey());
        dataStore.update(this);
    }
    public void list(){
        keySet.forEach((k)->{
            InventoryItem inventoryItem = new InventoryItem();
            inventoryItem.distributionKey(k);
            if(dataStore.load(inventoryItem)){
                itemList.add(inventoryItem);
            }
        });
    }
    @Override
    public int getFactoryId() {
        return ItemPortableRegistry.OID;
    }

    @Override
    public Map<String,Object> toMap(){
        super.toMap();
        return properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        super.fromMap(properties);
    }

    @Override
    public int getClassId() {
        return ItemPortableRegistry.INVENTORY_CID;
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("successful",true);
        jsonObject.addProperty("name",label);
        JsonArray items = new JsonArray();
        itemList.forEach((item)->items.add(item.toJson()));
        jsonObject.add("itemList",items);
        return jsonObject;
    }
}