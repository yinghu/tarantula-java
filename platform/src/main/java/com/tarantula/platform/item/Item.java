package com.tarantula.platform.item;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;

import java.util.ArrayList;
import java.util.List;

public class Item extends ConfigurableObject{


    public Item(){}

    public Item(ConfigurableObject configurableObject){
       super(configurableObject);
    }

    @Override
    public int getClassId() {
        return ItemPortableRegistry.ITEM_CID;
    }
    @Override
    public JsonObject toJson(){
        return super.toJson(super.toJson());
    }

    @Override
    public boolean configureAndValidate(JsonObject config){
        return super.configureAndValidate(config) && _validate();
    }

    @Override
    public boolean configureAndValidate(){
        int passed = 0;//have to have at least one reference
        for(JsonElement je : this.reference){
            ConfigurableObject cob = new ConfigurableObject();
            cob.distributionId(je.getAsLong());
            if(dataStore.load(cob)){
                passed++;
            }
            else{
                passed=0;//invalid reference
                break;
            }
        }
        return passed>0;
    }
    @Override
    public  <T extends Configurable> T setup(){
        _reference = new ArrayList<>();
        for(JsonElement je : reference){
            ConfigurableObject cob = new ConfigurableObject();
            cob.distributionId(je.getAsLong());
            cob.dataStore(dataStore);
            if(this.dataStore.load(cob)){
                cob.registerListener(this.listener);
                _reference.add(cob.setup());
            }
        }
        return (T)this;
    }

    public List<ConfigurableObject> list(){
        return _reference;
    }

    private boolean _validate(){
        if(this.configurationType.equals(Configurable.ITEM_CONFIG_TYPE)) return true;
        if(this.configurationType.endsWith(".")) return false;
        String[] comp = this.configurationType.split("\\.");
        if(comp.length != 2) return false; //asset.xxx
        return comp[0].equals(Configurable.ITEM_CONFIG_TYPE);
    }
}
