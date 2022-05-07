package com.tarantula.platform.store;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tarantula.platform.item.Application;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.item.ItemPortableRegistry;


import java.util.List;

public class Shop extends Application {



    public Shop(){
    }
    public Shop(ConfigurableObject configurableObject){
        super(configurableObject);
    }
    public Shop(String name){
        this();
        configurationName = name;
    }

    public String name(){
        return configurationName;
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name",configurationName);
        if(_reference==null) {
            jsonObject.addProperty("successful",false);
            return jsonObject;
        }
        jsonObject.addProperty("successful",true);
        JsonArray alist = new JsonArray();
        _reference.forEach((v)->{
            alist.add(v.toJson());
        });
        jsonObject.add("itemList",alist);
        return jsonObject;
    }

    public int getFactoryId() {
        return ItemPortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return ItemPortableRegistry.SHOP_CID;
    }

    public List<ConfigurableObject> list(){
        return _reference;
    }


}
