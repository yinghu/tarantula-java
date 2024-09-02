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
        application.addProperty("Successful",true);
        return application;
    }

    public static Shop build(JsonObject payload){
        Shop shop = new Shop();
        shop.application = payload;
        shop.configurationName = payload.get("ConfigurationName").getAsString();
        return shop;
    }

    public List<ShoppingItem> itemList(){
        ArrayList<ShoppingItem> items = new ArrayList<>();
        application.get("_shoppingItemList").getAsJsonArray().forEach(e->{
            JsonObject jo = e.getAsJsonObject();
            items.add(ShoppingItem.build(jo));
        });
        return items;
    }


}
