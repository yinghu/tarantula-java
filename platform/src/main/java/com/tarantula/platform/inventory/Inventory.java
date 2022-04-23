package com.tarantula.platform.inventory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Balance;
import com.icodesoftware.Configurable;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.item.ItemPortableRegistry;

import java.util.HashMap;
import java.util.Map;

public class Inventory extends IndexSet implements Configurable, Balance {

    private HashMap<String,InventoryItem> itemList = new HashMap<>();
    private boolean rechargeable;
    private double balance;
    //private String icon;

    public Inventory(){}

    public Inventory(String category){
        super("Inventory/"+category);
    }

    public Inventory(String category,boolean rechargeable){
        super("Inventory/"+category);
        //this.name = name;
        //this.icon = icon;
        this.rechargeable = rechargeable;
    }

    public void redeem(ApplicationRedeemer commodity){
        InventoryItem inventoryItem = new InventoryItem(commodity);
        dataStore.create(inventoryItem);
        keySet.add(inventoryItem.distributionKey());
        if(this.rechargeable){
            balance += commodity.amount();
        }
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
        //properties.put("name",name);
        //properties.put("icon",icon);
        properties.put("rechargeable",rechargeable);
        properties.put("balance",balance);
        return  super.toMap();
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        //name = (String) properties.remove("name");
        //icon = (String) properties.remove("icon");
        rechargeable = (boolean)properties.remove("rechargeable");
        balance = ((Number)properties.remove("balance")).doubleValue();
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
        //jsonObject.addProperty("name",name);
        //jsonObject.addProperty("icon",icon);
        jsonObject.addProperty("type",label.split("/")[1]);
        jsonObject.addProperty("balance",balance);
        jsonObject.addProperty("rechargeable",rechargeable);
        JsonArray items = new JsonArray();
        itemList.forEach((k,item)->items.add(item.toJson()));
        jsonObject.add("itemList",items);
        return jsonObject;
    }

    @Override
    public double balance() {
        return balance;
    }

    @Override
    public boolean transact(double amount) {
        return false;
    }
}