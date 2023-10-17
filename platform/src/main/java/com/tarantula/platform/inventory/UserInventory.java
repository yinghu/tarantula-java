package com.tarantula.platform.inventory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Inventory;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.item.ItemPortableRegistry;

import java.util.ArrayList;
import java.util.List;


public class UserInventory extends RecoverableObject implements Inventory {

    public final static String LABEL = "inventory";
    private ArrayList<Stock> itemList = new ArrayList<>();
    private boolean rechargeable;
    private boolean constrained;
    private double balance;
    private int count;

    public String typeId;
    public String type;

    private Listener listener;

    public UserInventory(){
        this.onEdge = true;
        this.label = LABEL;
    }

    public UserInventory(String category, String typeId){
        this(category,typeId,false);
    }

    public UserInventory(String category, String typeId, boolean rechargeable){
        this();
        this.type = category;
        this.typeId = typeId;
        this.rechargeable = rechargeable;
    }
    public UserInventory(String category, String typeId, boolean rechargeable,boolean constrained,Listener listener){
        this();
        this.type = category;
        this.typeId = typeId;
        this.rechargeable = rechargeable;
        this.constrained = constrained;
        this.listener = listener;
    }

    public void redeem(ApplicationRedeemer commodity,Inventory.Listener inventoryListener){
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

    public void redeem(ApplicationRedeemer commodity){
        InventoryItem inventoryItem = new InventoryItem(commodity);
        inventoryItem.ownerKey(this.key());
        dataStore.create(inventoryItem);
        if(this.rechargeable){
            balance += commodity.amount();
        }
        count++;
        dataStore.update(this);
        listener.onInventory(this,inventoryItem);
    }

    public void list(){
         InventoryItemQuery query = new InventoryItemQuery(this.distributionId);
         itemList.addAll(dataStore.list(query));
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
        constrained = buffer.readBoolean();
        return true;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeUTF8(type);
        buffer.writeUTF8(typeId);
        buffer.writeDouble(balance);
        buffer.writeInt(count);
        buffer.writeBoolean(rechargeable);
        buffer.writeBoolean(constrained);
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
        jsonObject.addProperty("Constrained",constrained);
        jsonObject.addProperty("Count",count);
        JsonArray items = new JsonArray();
        itemList.forEach((item)->items.add(item.toJson()));
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

    @Override
    public boolean rechargeable() {
        return rechargeable;
    }

    public boolean constrained(){
        return this.constrained;
    }

    public List<Stock> onStock(){
        return itemList;
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public String typeId() {
        return typeId;
    }

    public void resetListener(Listener listener){
        this.listener = listener;
    }
}