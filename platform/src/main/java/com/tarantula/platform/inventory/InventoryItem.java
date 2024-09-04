package com.tarantula.platform.inventory;


import com.google.gson.JsonObject;
import com.icodesoftware.Inventory;
import com.icodesoftware.Recoverable;
import com.tarantula.platform.item.ConfigurableEdit;
import com.tarantula.platform.item.ItemPortableRegistry;


public class InventoryItem extends ConfigurableEdit implements Inventory.Stock {

    public final static String LABEL = "inventory_item";

    private long stockId;
    private long itemId;
    //private List<PropertyEdit> stock = new ArrayList<>();


    public InventoryItem(){
        this.onEdge = true;
        this.label = LABEL;
    }
    public InventoryItem(ApplicationRedeemer commodity,long stockId){
        this();
        this.configurationName = commodity.configurationName();
        this.configurationTypeId = commodity.configurationTypeId();
        this.itemId = commodity.distributionId();
        this.stockId = stockId;
    }
    public InventoryItem(long itemId,long stockId){
        this();
        this.itemId = itemId;
        this.stockId = stockId;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeUTF8(configurationTypeId);
        buffer.writeUTF8(configurationName);
        buffer.writeLong(itemId);
        buffer.writeLong(stockId);
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        this.configurationTypeId = buffer.readUTF8();
        this.configurationName = buffer.readUTF8();
        this.itemId = buffer.readLong();
        this.stockId = buffer.readLong();
        return true;
    }

    public int getFactoryId() {
        return ItemPortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return ItemPortableRegistry.INVENTORY_ITEM_CID;
    }

    @Override
    public JsonObject toJson() {
        //return assembly();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("InventoryId",distributionKey());
        jsonObject.addProperty("TypeId",configurationTypeId);
        jsonObject.addProperty("Name",configurationName);
        jsonObject.addProperty("ItemId",Long.toString(itemId));
        jsonObject.addProperty("StockId",Long.toString(stockId));
        /**
        stock.forEach(prop->{
            if(prop.type.equals("number")){
                jsonObject.addProperty(prop.name(),prop.edit.getAsNumber());
            }
            else if(prop.type.equals("enum")){
                jsonObject.addProperty(prop.name(),prop.edit.getAsInt());
            }
            else if(prop.type.equals("string")){
                jsonObject.addProperty(prop.name(),prop.edit.getAsString());
            }
            else if(prop.type.equals("category") || prop.type.equals("list")){
                jsonObject.add(prop.name(),prop.edit.getAsJsonArray());
            }
        });**/
        return jsonObject;
    }

    public long itemId(){
        return itemId;
    }
    public long stockId(){
        return stockId;
    }

    public void stock(Recoverable recoverable){
        //stock.add((PropertyEdit)recoverable);
    }

}
