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
    protected JsonObject payload = new JsonObject();

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
        this.properties.put("4",this.payload.toString());
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.configurationType = (String)properties.get("1");
        this.configurationName = (String)properties.get("2");
        this.configurationCategory = (String)properties.get("3");
        this.payload = JsonUtil.parse((String)properties.get("4"));
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
        jsonObject.addProperty("type",configurationType);
        jsonObject.addProperty("name",configurationName);
        jsonObject.addProperty("category",configurationCategory);
        jsonObject.addProperty("itemId",distributionKey());
        jsonObject.add("payload",payload);
        return jsonObject;
    }
    public boolean configureAndValidate(byte[] data){
        this.payload = JsonUtil.parse(data);
        return true;
    }

}
