package com.tarantula.platform.inventory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Balance;
import com.icodesoftware.Configurable;
import com.icodesoftware.Countable;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.item.ItemPortableRegistry;

import java.util.HashMap;
import java.util.Map;

public class Inventory extends IndexSet implements Configurable, Balance, Countable {

    private HashMap<String,InventoryItem> itemList = new HashMap<>();
    private boolean rechargeable;
    private double balance;
    private int count;

    public Inventory(){}

    public Inventory(String category,String typeId){
        super("Inventory/"+category+"/"+typeId);
    }

    public Inventory(String category,String typeId,boolean rechargeable){
        super("Inventory/"+category+"/"+typeId);
        this.rechargeable = rechargeable;
    }

    public void redeem(ApplicationRedeemer commodity){
        InventoryItem inventoryItem = new InventoryItem(commodity);
        dataStore.create(inventoryItem);
        keySet.add(inventoryItem.distributionKey());
        if(this.rechargeable){
            balance += commodity.amount();
        }
        count++;
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
        properties.put("rechargeable",rechargeable);
        properties.put("balance",balance);
        properties.put("count",count);
        return  super.toMap();
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        rechargeable = (boolean)properties.remove("rechargeable");
        balance = ((Number)properties.remove("balance")).doubleValue();
        count = ((Number)properties.remove("count")).intValue();
        super.fromMap(properties);
    }

    @Override
    public int getClassId() {
        return ItemPortableRegistry.INVENTORY_CID;
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Successful",true);
        String[] ttp = label.split("/");
        jsonObject.addProperty("Type",ttp[1]);
        jsonObject.addProperty("TypeId",ttp[2]);
        jsonObject.addProperty("Balance",Double.valueOf(balance).intValue());
        jsonObject.addProperty("Rechargeable",rechargeable);
        jsonObject.addProperty("Count",count);
        JsonArray items = new JsonArray();
        itemList.forEach((k,item)->items.add(item.toJson()));
        jsonObject.add("_itemList",items);
        return jsonObject;
    }

    @Override
    public double balance() {
        return balance;
    }

    @Override
    public boolean transact(double amount) {
        if(!rechargeable || amount==0) return false;
        if(amount>0) {
            balance += amount;
            this.dataStore.update(this);
            return true;
        }
        double remaining = balance-(amount*(-1));
        if(remaining<0) return false;
        balance -= amount*(-1);
        this.dataStore.update(this);
        return true;
    }
    public int count(int delta){
        count += delta;
        return count;
    }
}