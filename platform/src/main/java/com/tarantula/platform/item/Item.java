package com.tarantula.platform.item;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;

import java.util.ArrayList;
import java.util.Map;

public class Item extends ConfigurableObject{

    private ArrayList<ConfigurableObject> _reference;
    public Item(){}

    public Item(ConfigurableObject configurableObject){
       super(configurableObject);
    }

    @Override
    public Map<String,Object> toMap(){
        super.toMap();
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        super.fromMap(properties);
    }

    @Override
    public int getClassId() {
        return ItemPortableRegistry.ITEM_CID;
    }
    @Override
    public JsonObject toJson(){
        JsonObject json = super.toJson();
        _reference.forEach((cob)->{
            json.add(cob.configurationName,cob.toJson());
        });
        return json;
    }

    @Override
    public boolean configureAndValidate(JsonObject config){
        return super.configureAndValidate(config)&&this.configurationType.equals(Configurable.ITEM_CONFIG_TYPE);
    }

    @Override
    public boolean configureAndValidate(){
        int passed = 0;
        for(JsonElement je : this.reference){
            ConfigurableObject cob = new ConfigurableObject();
            cob.distributionKey(je.getAsString());
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
            cob.distributionKey(je.getAsString());
            cob.dataStore(dataStore);
            if(this.dataStore.load(cob)){
                _reference.add(cob.setup());
            }
        }
        return (T)this;
    }
}
