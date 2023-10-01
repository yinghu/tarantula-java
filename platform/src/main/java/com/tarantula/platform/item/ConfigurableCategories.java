package com.tarantula.platform.item;

import com.google.gson.JsonArray;

import com.google.gson.JsonObject;
import com.icodesoftware.Configuration;
import com.icodesoftware.util.RecoverableObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigurableCategories extends RecoverableObject implements Configuration {

    private static String ITEM_LIST = "itemList";
    private JsonObject application = new JsonObject();
    private ConfigurableTypes configurableTypes;
    private ConcurrentHashMap<String,ConfigurableCategory> categories = new ConcurrentHashMap<>();


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
        ArrayList<ConfigurableCategory> list = new ArrayList<>();
        categories.forEach((k,v)->list.add(v));
        return list;
    }

    public boolean addCategory(ConfigurableCategory category){
        return categories.putIfAbsent(category.name(),category)==null;
    }

    public void configurableTypes(ConfigurableTypes configurableTypes){
        this.configurableTypes = configurableTypes;
    }
    public ConfigurableCategory configurableSetting(String category){
        return categories.get(category);

    }

}
