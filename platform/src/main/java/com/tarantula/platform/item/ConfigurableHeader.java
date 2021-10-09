package com.tarantula.platform.item;

import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;

public class ConfigurableHeader extends ConfigurableObject {

    public ConfigurableHeader(){}

    public ConfigurableHeader(ConfigurableObject configurableObject){
        super(configurableObject);
    }
    @Override
    public int getClassId() {
        return ItemPortableRegistry.CONFIGURABLE_HEADER_CID;
    }
    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("itemId", distributionKey());
        jsonObject.addProperty("configurationType", configurationType);
        jsonObject.addProperty("configurationTypeId", configurationTypeId);
        jsonObject.addProperty("configurationName", configurationName);
        jsonObject.addProperty("configurationCategory", configurationCategory);
        jsonObject.addProperty("configurationVersion", configurationVersion);
        jsonObject.addProperty("disabled",disabled);
        jsonObject.add("header",header);
        jsonObject.add("application",application);
        jsonObject.add("payload",payload);
        jsonObject.add("reference",reference);
        return jsonObject;
    }
    @Override
    public  <T extends Configurable> T setup(){
        return (T)this;
    }
}