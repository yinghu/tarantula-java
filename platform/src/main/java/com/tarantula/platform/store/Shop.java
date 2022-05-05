package com.tarantula.platform.store;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tarantula.platform.item.GrantableObject;

import java.util.List;

public class Shop extends GrantableObject {

    private List<ShoppingItem> itemList;

    public Shop(List<ShoppingItem> itemList){
        this.itemList = itemList;
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        JsonArray alist = new JsonArray();
        itemList.forEach((v)->{
            alist.add(v.toJson());
        });
        jsonObject.add("itemList",alist);
        return jsonObject;
    }

}
