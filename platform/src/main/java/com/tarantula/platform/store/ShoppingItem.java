package com.tarantula.platform.store;

import com.google.gson.JsonObject;
import com.tarantula.platform.item.*;


public class ShoppingItem extends GrantableObject{

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
        JsonObject json = super.toJson();
        _reference.forEach((cob)-> json.add(cob.distributionKey(),cob.toJson()));
        return json;
    }

}
