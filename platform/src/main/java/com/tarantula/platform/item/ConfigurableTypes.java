package com.tarantula.platform.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Configuration;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.NaturalKey;
import com.icodesoftware.util.RecoverableObject;

import java.util.Map;

public class ConfigurableTypes extends RecoverableObject implements Configuration {

    private static String ITEM_LIST = "itemList";
    private JsonObject application = new JsonObject();

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
        return ItemPortableRegistry.CONFIGURABLE_TYPES_CID;
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name",name);
        jsonObject.add(ITEM_LIST,application.get(ITEM_LIST));
        return jsonObject;
    }

    public JsonArray toTypes(){
        if(!application.has(ITEM_LIST)) application.add(ITEM_LIST,new JsonArray());
        return application.get(ITEM_LIST).getAsJsonArray();
    }
    public void addType(JsonObject type){
        if(!application.has(ITEM_LIST)){
            application.add(ITEM_LIST,new JsonArray());
        }
        removeItem(type).add(type);
    }
    public void removeType(JsonObject type){
        if(!application.has(ITEM_LIST)){
            return;
        }
        removeItem(type);
    }
    public Key key(){
        return new NaturalKey("category/types/"+name);
    }

    private JsonArray removeItem(JsonObject type){
        JsonArray items = application.get(ITEM_LIST).getAsJsonArray();
        JsonElement existing = null;
        for(JsonElement je : items) {
            if (je.getAsJsonObject().get("name").getAsString().equals(type.get("name").getAsString())){
                existing = je;
                break;
            }
        }
        if(existing !=null){
            items.remove(existing);
        }
        return items;
    }
}
