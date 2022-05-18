package com.tarantula.platform.store;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tarantula.platform.item.*;


public class ShoppingItem extends Item{


    public ShoppingItem(){

    }

    public ShoppingItem(ConfigurableObject configurableObject){
        super(configurableObject);
    }

    public int getFactoryId() {
        return ItemPortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return ItemPortableRegistry.SHOPPING_ITEM_CID;
    }

    public String name(){
        return configurationName();
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        JsonArray list = new JsonArray();
        if(_reference!=null) {
            _reference.forEach((cob)-> list.add(cob.toJson()));
        }
        json.add("itemList",list);
        return json;
    }

}
