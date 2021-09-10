package com.tarantula.platform.inventory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.item.ItemPortableRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Inventory extends IndexSet implements Configurable {

    private HashMap<String,InventoryItem> itemList = new HashMap<>();


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
    public Configurable load(String inventoryId){
        InventoryItem inventoryItem = itemList.get(inventoryId);
        if(inventoryItem==null){
            return null;
        }
        return inventoryItem.load();
    }
    public void list(){
        keySet.forEach((k)->{
            InventoryItem inventoryItem = new InventoryItem();
            inventoryItem.distributionKey(k);
            if(dataStore.load(inventoryItem)){
                inventoryItem.dataStore(dataStore);
                itemList.put(k,inventoryItem);
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
        jsonObject.addProperty("name",label.split("/")[1]);
        JsonArray items = new JsonArray();
        itemList.forEach((k,item)->items.add(item.toJson()));
        jsonObject.add("itemList",items);
        return jsonObject;
    }
}