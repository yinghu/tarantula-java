package com.tarantula.platform.inventory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Balance;
import com.icodesoftware.Configurable;
import com.icodesoftware.Countable;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.item.ItemPortableRegistry;

import java.util.HashMap;
import java.util.Map;

public class Inventory extends RecoverableObject implements Configurable, Balance, Countable {

    public final static String LABEL = "inventory";
    private HashMap<String,InventoryItem> itemList = new HashMap<>();
    private boolean rechargeable;
    private double balance;
    private int count;

    private String typeId;
    private String type;


    public Inventory(){
        this.onEdge = true;
        this.label = LABEL;
    }

    public Inventory(String category,String typeId){
        this(category,typeId,false);
    }

    public Inventory(String category,String typeId,boolean rechargeable){
        this();
        this.type = category;
        this.typeId = typeId;
        this.rechargeable = rechargeable;
    }

    public void redeem(ApplicationRedeemer commodity,InventoryListener inventoryListener){
        InventoryItem inventoryItem = new InventoryItem(commodity);
        inventoryItem.ownerKey(this.key());
        dataStore.create(inventoryItem);
        if(this.rechargeable){
            balance += commodity.amount();
        }
        count++;
        dataStore.update(this);
        inventoryListener.onInventory(this,inventoryItem);
    }
    public String load(String inventoryId){
        InventoryItem inventoryItem = itemList.get(inventoryId);
        if(inventoryItem==null){
            return null;
        }
        return inventoryItem.itemId();
    }

    public void list(){
        //keySet.forEach((k)->{
            InventoryItem inventoryItem = new InventoryItem();
            //inventoryItem.distributionKey(k);
            if(dataStore.load(inventoryItem)){
                inventoryItem.dataStore(dataStore);
                //itemList.put(k,inventoryItem);
            }
        //});
    }
    @Override
    public int getFactoryId() {
        return ItemPortableRegistry.OID;
    }


    @Override
    public boolean read(DataBuffer buffer) {
        type = buffer.readUTF8();
        typeId = buffer.readUTF8();
        balance = buffer.readDouble();
        count = buffer.readInt();
        rechargeable = buffer.readBoolean();
        return true;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeUTF8(type);
        buffer.writeUTF8(typeId);
        buffer.writeDouble(balance);
        buffer.writeInt(count);
        buffer.writeBoolean(rechargeable);
        return true;
    }

    @Override
    public int getClassId() {
        return ItemPortableRegistry.INVENTORY_CID;
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Successful",true);
        jsonObject.addProperty("Type",type);
        jsonObject.addProperty("TypeId",typeId);
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