package com.tarantula.platform.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Configuration;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.NaturalKey;
import com.icodesoftware.util.RecoverableObject;

import java.util.Iterator;
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
    public JsonArray toCategories(){
        if(!application.has(ITEM_LIST)) application.add(ITEM_LIST,new JsonArray());
        return application.get(ITEM_LIST).getAsJsonArray();
    }
    public boolean addCategory(JsonObject type){
        if(!application.has(ITEM_LIST)){
            application.add(ITEM_LIST,new JsonArray());
        }
        JsonArray items = application.get(ITEM_LIST).getAsJsonArray();
        boolean exiting = false;
        for(JsonElement je : items) {
            String ex = je.getAsJsonObject().get("header").getAsJsonObject().get("type").getAsString();
            String ax = type.getAsJsonObject().get("header").getAsJsonObject().get("type").getAsString();
            if (ex.equals(ax)){
                exiting = true;
                break;
            }
        }
        if(exiting) return false;
        items.add(type);
        return true;
    }
    public boolean updateCategory(JsonObject type){
        if(!application.has(ITEM_LIST)){
            application.add(ITEM_LIST,new JsonArray());
        }
        JsonArray items = application.get(ITEM_LIST).getAsJsonArray();
        boolean removed = false;
        for(Iterator<JsonElement> it = items.iterator(); it.hasNext();){
            JsonObject jo = it.next().getAsJsonObject();
            String ex = jo.get("header").getAsJsonObject().get("type").getAsString();
            String ax = type.get("header").getAsJsonObject().get("type").getAsString();
            if(ex.equals(ax)){
                it.remove();
                removed = true;
            }
        }
        if(!removed) return false;
        items.add(type);
        return true;
    }
    public boolean removeCategory(JsonObject type){
        if(!application.has(ITEM_LIST)){
            application.add(ITEM_LIST,new JsonArray());
        }
        JsonArray items = application.get(ITEM_LIST).getAsJsonArray();
        boolean removed = false;
        for(Iterator<JsonElement> it = items.iterator(); it.hasNext();){
            JsonObject jo = it.next().getAsJsonObject();
            String ex = jo.get("header").getAsJsonObject().get("type").getAsString();
            String ax = type.get("header").getAsJsonObject().get("type").getAsString();
            if(ex.equals(ax)){
                it.remove();
                removed = true;
            }
        }
        return removed;
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
                configurableSetting.scope = header.get("scope").getAsString();
                configurableSetting.version = header.get("version").getAsString();
                configurableSetting.description = header.get("description").getAsString();
                configurableSetting.rechargeable = header.get("rechargeable").getAsBoolean();
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
