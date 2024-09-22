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
        super(payload);
        this.header.addProperty("Successful",true);
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
        if(header.has("_shoppingItemList")){
            return header;
        }
        return super.toJson();
    }


    public List<ShoppingItem> itemList(){
        if(!header.has("_shoppingItemList")) return null;
        ArrayList<ShoppingItem> items = new ArrayList<>();
        header.get("_shoppingItemList").getAsJsonArray().forEach(e->{
            JsonObject jo = e.getAsJsonObject();
            items.add(new ShoppingItem(jo));
        });
        return items;
    }


}
