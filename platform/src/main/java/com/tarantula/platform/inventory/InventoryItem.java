package com.tarantula.platform.inventory;


import com.google.gson.JsonObject;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.item.ItemPortableRegistry;

import java.util.Map;

public class InventoryItem extends ConfigurableObject {

    public InventoryItem(){

    }
    public InventoryItem(ApplicationRedeemer commodity){
        this.configurationName = commodity.configurationName();
        this.configurationTypeId = commodity.configurationTypeId();
        this.reference.add(commodity.distributionKey());
    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put(TYPE_ID_KEY, this.configurationTypeId);
        this.properties.put(NAME_KEY, this.configurationName);
        this.properties.put(REFERENCE_KEY,reference.toString());
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.configurationTypeId = (String) properties.get(TYPE_ID_KEY);
        this.configurationName = (String) properties.get(NAME_KEY);
        this.reference = JsonUtil.parseAsJsonArray((String) properties.getOrDefault(REFERENCE_KEY, "[]"));
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
        return jsonObject;
    }

    public ConfigurableObject load(){
        ConfigurableObject configurableObject = new ConfigurableObject();
        configurableObject.distributionKey(reference.get(0).getAsString());
        if(!dataStore.load(configurableObject)) return null;
        configurableObject.dataStore(dataStore);
        return configurableObject.setup();
    }

}
