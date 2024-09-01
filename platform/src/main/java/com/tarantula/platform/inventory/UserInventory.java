package com.tarantula.platform.inventory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Inventory;
import com.icodesoftware.Recoverable;
import com.icodesoftware.service.ApplicationPreSetup;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.item.Commodity;
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
    private int stockFactoryId;
    private int stockClassId;
    private Listener listener;
    private ApplicationPreSetup applicationPreSetup;
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

    public void redeem(ApplicationRedeemer commodity){
        InventoryItem inventoryItem = new InventoryItem(commodity,applicationPreSetup.distributionId());
        inventoryItem.ownerKey(this.key());
        dataStore.create(inventoryItem);
        if(this.rechargeable){
            balance += commodity.amount();
        }
        count++;
        dataStore.update(this);
        listener.onInventory(this.applicationPreSetup,this,inventoryItem);
    }

    public void redeem(Commodity commodity){
        InventoryItem inventoryItem = new InventoryItem(commodity,applicationPreSetup.distributionId());
        inventoryItem.ownerKey(this.key());
        dataStore.create(inventoryItem);
        if(this.rechargeable){
            balance += commodity.application().get("Amount").getAsDouble();
        }
        count++;
        dataStore.update(this);
        listener.onInventory(this.applicationPreSetup,this,inventoryItem);
    }

    public void list(){
         InventoryItemQuery query = new InventoryItemQuery(this.distributionId);
         dataStore.list(query).forEach(inventoryItem -> {
             if(!rechargeable){
                 Recoverable stock = applicationPreSetup.create(stockFactoryId,stockClassId);
                 stock.distributionId(inventoryItem.stockId());
                 if(dataStore.load(stock)){
                     inventoryItem.stock(stock);
                 }
             }
             itemList.add(inventoryItem);
         });
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
        stockFactoryId = buffer.readInt();
        stockClassId = buffer.readInt();
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
        buffer.writeInt(stockFactoryId);
        buffer.writeInt(stockClassId);
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
        if(rechargeable) return jsonObject;
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
    public void applicationPreSetup(ApplicationPreSetup applicationPreSetup){
        this.applicationPreSetup = applicationPreSetup;
    }

    public int stockFactoryId(){
        return stockFactoryId;
    }
    public int stockClassId(){
        return stockClassId;
    }

    public void stockFactoryId(int stockFactoryId){
        this.stockFactoryId = stockFactoryId;
    }
    public void stockClassId(int stockClassId){
        this.stockClassId = stockClassId;
    }

    public Stock stock(long stockId){
        InventoryItem inventoryItem = new InventoryItem();
        inventoryItem.distributionId(stockId);
        return dataStore.load(inventoryItem) ? inventoryItem : null;
    }
    public void removeStock(Stock stock){
        if(rechargeable) return;
        if(this.dataStore.delete(stock)){
            count--;
            this.dataStore.update(this);
        }
    }
}