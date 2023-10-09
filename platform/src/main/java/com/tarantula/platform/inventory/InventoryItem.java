package com.tarantula.platform.inventory;


import com.google.gson.JsonObject;
import com.icodesoftware.Inventory;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.item.ItemPortableRegistry;


public class InventoryItem extends ConfigurableObject implements Inventory.Stock {

    public final static String LABEL = "inventory_item";

    private JsonObject commodity;
    public InventoryItem(){
        this.onEdge = true;
        this.label = LABEL;
    }
    public InventoryItem(ApplicationRedeemer commodity){
        this();
        this.configurationName = commodity.configurationName();
        this.configurationTypeId = commodity.configurationTypeId();
        this.reference.add(commodity.distributionKey());
        this.commodity = commodity.toJson();
    }


    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeUTF8(configurationTypeId);
        buffer.writeUTF8(configurationName);
        buffer.writeUTF8(reference!=null?reference.toString():"[]");
        buffer.writeUTF8(commodity!=null?commodity.toString():"{}");
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        this.configurationTypeId = buffer.readUTF8();
        this.configurationName = buffer.readUTF8();
        this.reference = JsonUtil.parseAsJsonArray(buffer.readUTF8());
        this.commodity = JsonUtil.parse(buffer.readUTF8());
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
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("InventoryId",distributionKey());
        jsonObject.addProperty("TypeId",configurationTypeId);
        jsonObject.addProperty("Name",configurationName);
        jsonObject.add("_commodity",commodity);
        return jsonObject;
    }

    //public ConfigurableObject load(){
        //ConfigurableObject configurableObject = new ConfigurableObject();
        //configurableObject.distributionKey(reference.get(0).getAsString());
        //if(!dataStore.load(configurableObject)) return null;
        //configurableObject.dataStore(dataStore);
        //return configurableObject.setup();
    //}

    public long stockId(){
        return reference.get(0).getAsLong();
    }

}
