package com.tarantula.platform.item;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;

import java.util.ArrayList;
import java.util.Map;

public class Commodity extends ConfigurableObject{

    private ArrayList<ConfigurableObject> _reference;
    public Commodity(){}

    public Commodity(ConfigurableObject configurableObject){
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
        return ItemPortableRegistry.COMMODITY_CID;
    }

    @Override
    public boolean configureAndValidate(JsonObject config){
        return super.configureAndValidate(config)&&this.configurationType.equals(Configurable.COMMODITY_CONFIG_TYPE);
    }
    @Override
    public boolean configureAndValidate(){
        boolean passed = true;
        for(JsonElement je : this.reference){
            ConfigurableObject cob = new ConfigurableObject();
            cob.distributionKey(je.getAsString());
            if(!dataStore.load(cob)){
                passed = false;
                break;
            }
        }
        return passed;
    }
    @Override
    public JsonObject toJson() {
        JsonObject json = super.toJson();
        setup();
        _reference.forEach((cob)->{
            json.add(cob.distributionKey(),cob.toJson());
        });
        return json;
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
