package com.tarantula.platform.store;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.item.GrantableObject;
import com.tarantula.platform.item.ItemPortableRegistry;

import java.util.List;

public class Shop extends GrantableObject {

    private List<ShoppingItem> itemList;

    public Shop(){

    }
    public Shop(ConfigurableObject configurableObject){
        super(configurableObject);
    }
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

    public int getFactoryId() {
        return ItemPortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return ItemPortableRegistry.SHOP_CID;
    }

}
