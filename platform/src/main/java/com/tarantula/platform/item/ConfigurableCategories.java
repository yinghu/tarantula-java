package com.tarantula.platform.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Configuration;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.NaturalKey;
import com.icodesoftware.util.RecoverableObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigurableCategories extends RecoverableObject implements Configuration {

    private static String ITEM_LIST = "itemList";
    private JsonObject application = new JsonObject();
    private ConfigurableTypes configurableTypes;
    private ConcurrentHashMap<String,ConfigurableCategory> categories = new ConcurrentHashMap<>();
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
    public boolean read(DataBuffer buffer){
        this.name = buffer.readUTF8();
        this.application = JsonUtil.parse(buffer.readUTF8());
        return true;
    }
    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeUTF8(name);
        buffer.writeUTF8(application.toString());
        return true;
    }
    @Override
    public int getClassId() {
        return ItemPortableRegistry.CONFIGURABLE_CATEGORIES_CID;
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name",name);
        JsonArray items = new JsonArray();
        categories.forEach((k,v)-> items.add(v.toJson()));
        jsonObject.add(ITEM_LIST,items);
        if(configurableTypes!=null) jsonObject.add("types",configurableTypes.toJson());
        return jsonObject;
    }
    public List<ConfigurableCategory> toCategories(){
        //if(!application.has(ITEM_LIST)) application.add(ITEM_LIST,new JsonArray());
        //return application.get(ITEM_LIST).getAsJsonArray();
        ArrayList<ConfigurableCategory> list = new ArrayList<>();
        categories.forEach((k,v)->list.add(v));
        return list;
    }

    public boolean addCategory(ConfigurableCategory category){
        return categories.putIfAbsent(category.name(),category)==null;
    }
    /**
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
    }**/
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
    public ConfigurableCategory configurableSetting(String category){
        //ConfigurableCategory config = categories.get(category);
        //if(config==null) return null;
        //JsonArray items = application.get(ITEM_LIST).getAsJsonArray();
        //ConfigurableSetting configurableSetting = new ConfigurableSetting();
        //for(JsonElement je : items) {
            //JsonObject item = je.getAsJsonObject();
            //JsonObject header = item.getAsJsonObject().get("header").getAsJsonObject();
            //if(header.get("type").getAsString().equals(category)){
        //configurableSetting.type = category;
          //      configurableSetting.scope = config.header.get("scope").getAsString();
            //    configurableSetting.version = header.get("version").getAsString();
              //  configurableSetting.description = header.get("description").getAsString();
               // configurableSetting.rechargeable = header.get("rechargeable").getAsBoolean();
               // configurableSetting.properties = item.get("application").getAsJsonObject().get("properties").getAsJsonArray();

        return categories.get(category);

    }
    public Key key(){
        return new NaturalKey("category/classes/"+name);//name => one of asset, component, item, application
    }

}
