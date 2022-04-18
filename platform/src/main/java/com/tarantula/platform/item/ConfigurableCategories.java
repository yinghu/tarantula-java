package com.tarantula.platform.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Configuration;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.NaturalKey;
import com.icodesoftware.util.RecoverableObject;

import java.util.Map;

public class ConfigurableCategories extends RecoverableObject implements Configuration {

    private static String ITEM_LIST = "itemList";
    private JsonObject application = new JsonObject();
    private ConfigurableTypes configurableTypes;
    @Override
    public int getFactoryId() {
        return ItemPortableRegistry.OID;
    }

    @Override
    public Map<String,Object> toMap(){
        properties.put("name",name);
        properties.put("application",application.toString());
        return properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.name = (String) properties.get("name");
        this.application = JsonUtil.parse((String)properties.getOrDefault("application","{}"));
    }

    @Override
    public int getClassId() {
        return ItemPortableRegistry.CONFIGURABLE_CATEGORIES_CID;
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name",name);
        if(!application.has(ITEM_LIST)) application.add(ITEM_LIST,new JsonArray());
        jsonObject.add(ITEM_LIST,application.get(ITEM_LIST));
        if(configurableTypes!=null) jsonObject.add("types",configurableTypes.toJson());
        return jsonObject;
    }
    public void addType(JsonObject type){
        if(!application.has(ITEM_LIST)){
            application.add(ITEM_LIST,new JsonArray());
        }
        JsonArray items = application.get(ITEM_LIST).getAsJsonArray();
        items.add(type);
    }
    public void configurableTypes(ConfigurableTypes configurableTypes){
        this.configurableTypes = configurableTypes;
    }
    public ConfigurableSetting configurableSetting(String category){
        if(!application.has(ITEM_LIST)) return null;
        JsonArray items = application.get(ITEM_LIST).getAsJsonArray();
        ConfigurableSetting configurableSetting = null;
        for(JsonElement je : items) {
            JsonObject item = je.getAsJsonObject();
            JsonObject header = item.getAsJsonObject().get("header").getAsJsonObject();
            if(header.get("type").getAsString().equals(category)){
                configurableSetting = new ConfigurableSetting();
                configurableSetting.type = category;
                configurableSetting.name = header.get("name").getAsString();
                configurableSetting.icon = header.get("icon").getAsString();
                configurableSetting.properties = item.get("application").getAsJsonObject().get("properties").getAsJsonArray();
                break;
            }
        }
        return configurableSetting;

    }
    public Key key(){
        return new NaturalKey("category/classes/"+name);
    }

}
