package com.tarantula.platform.store;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;
import com.tarantula.platform.item.*;

import java.util.ArrayList;


public class ShoppingItem extends GrantableObject{

    private ArrayList<ConfigurableObject> _reference;

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
        _setup();
        _reference.forEach((cob)-> json.add(cob.distributionKey(),cob.toJson()));
        return json;
    }

    public  <T extends Configurable> T _setup(){
        _reference = new ArrayList<>();
        for(JsonElement je : reference){
            ConfigurableObject cob = new ConfigurableObject();
            cob.distributionKey(je.getAsString());
            cob.dataStore(dataStore);
            if(this.dataStore.load(cob)){
                _reference.add(cob.setup());
            }
        }
        return (T)this;
    }

}
