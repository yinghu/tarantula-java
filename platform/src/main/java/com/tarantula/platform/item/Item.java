package com.tarantula.platform.item;

import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.RecoverableObject;

import java.util.Map;

public class Item extends RecoverableObject implements Configurable {

    protected String configurationType;
    protected String configurationName;
    protected String configurationCategory;
    protected JsonObject header = new JsonObject();
    protected JsonObject payload = new JsonObject();
    protected JsonObject application = new JsonObject();
    protected JsonObject reference = new JsonObject();
    public String configurationType(){return this.configurationType;}
    public void configurationType(String configurationType){
        this.configurationType = configurationType;
    }
    public String configurationName(){return configurationName;}
    public void configurationName(String configurationName){
        this.configurationName = configurationName;
    }
    public String configurationCategory(){return configurationCategory;}
    public void configurationCategory(String configurationCategory){
        this.configurationCategory = configurationCategory;
    }
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",this.configurationType);
        this.properties.put("2",this.configurationName);
        this.properties.put("3",this.configurationCategory);
        this.properties.put("4",this.header.toString());
        this.properties.put("5",this.application.toString());
        this.properties.put("6",this.payload.toString());
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.configurationType = (String)properties.get("1");
        this.configurationName = (String)properties.get("2");
        this.configurationCategory = (String)properties.get("3");
        this.header = JsonUtil.parse((String)properties.get("4"));
        this.application = JsonUtil.parse((String)properties.get("5"));
        this.payload = JsonUtil.parse((String)properties.get("6"));
    }
    public int getFactoryId() {
        return ItemPortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return ItemPortableRegistry.ITEM_CID;
    }
    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("itemId",distributionKey());
        JsonObject header = new JsonObject();
        header.addProperty("configurationType",configurationType);
        header.addProperty("configurationName",configurationName);
        header.addProperty("configurationCategory",configurationCategory);
        jsonObject.add("header",header);
        jsonObject.add("application",application);
        jsonObject.add("payload",payload);
        jsonObject.add("reference",reference);
        return jsonObject;
    }
    public boolean configureAndValidate(byte[] data){
        JsonObject config = JsonUtil.parse(data);
        this.header = config.getAsJsonObject("header");
        this.configurationType = header.get("configurationType").getAsString();
        this.configurationName = header.get("configurationName").getAsString();
        this.configurationCategory = header.get("configurationCategory").getAsString();
        if(config.has("application")) this.application = config.getAsJsonObject("application");
        if(config.has("payload")) this.payload = config.getAsJsonObject("payload");
        if(config.has("reference")) this.reference = config.getAsJsonObject("reference");
        return true;
    }
}
