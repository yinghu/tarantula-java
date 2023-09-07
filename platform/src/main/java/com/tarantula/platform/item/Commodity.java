package com.tarantula.platform.item;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;

import java.util.ArrayList;
import java.util.Map;

public class Commodity extends ConfigurableObject{

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
        return super.configureAndValidate(config) && _validate();
    }
    @Override
    public boolean configureAndValidate(){
        boolean passed = true;
        for(JsonElement je : this.reference){
            ConfigurableObject cob = new ConfigurableObject();
            cob.distributionId(je.getAsLong());
            if(!dataStore.load(cob)){
                passed = false;
                break;
            }
        }
        return passed;
    }
    @Override
    public JsonObject toJson() {
        JsonObject json = super.toJson(super.toJson());
        json.addProperty("Successful",true);
        return json;
    }
    @Override
    public  <T extends Configurable> T setup(){
        if(this.listener!=null) listener.onLoaded(this);
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

    private boolean _validate(){
        if(this.configurationType.equals(Configurable.COMMODITY_CONFIG_TYPE)) return true;
        if(this.configurationType.endsWith(".")) return false;
        String[] comp = this.configurationType.split("\\.");
        if(comp.length != 2) return false; //asset.xxx
        return comp[0].equals(Configurable.COMMODITY_CONFIG_TYPE);
    }

}
