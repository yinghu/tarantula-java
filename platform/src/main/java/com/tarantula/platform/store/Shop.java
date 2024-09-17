package com.tarantula.platform.store;

import com.google.gson.JsonObject;
import com.tarantula.platform.item.Application;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.item.ItemPortableRegistry;


import java.util.ArrayList;
import java.util.List;

public class Shop extends Application {

    public Shop(){
    }

    public Shop(String name){
        this();
        configurationName = name;
    }

    public Shop(JsonObject payload){
        this.application = payload;
        this.configurationName = payload.get("ConfigurationName").getAsString();
    }

    public String name(){
        return configurationName;
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

    @Override
    public JsonObject toJson() {
        if(application.has("_shoppingItemList")) return application;
        return super.toJson();
    }


    public List<ShoppingItem> itemList(){
        if(!application.has("_shoppingItemList")) return null;
        ArrayList<ShoppingItem> items = new ArrayList<>();
        application.get("_shoppingItemList").getAsJsonArray().forEach(e->{
            JsonObject jo = e.getAsJsonObject();
            items.add(new ShoppingItem(jo));
        });
        return items;
    }


}
