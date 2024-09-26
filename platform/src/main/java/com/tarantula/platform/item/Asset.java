package com.tarantula.platform.item;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;

import java.util.ArrayList;
import java.util.Map;

public class Asset extends ConfigurableObject{

    public Asset(){}

    public Asset(ConfigurableObject configurableObject){
        super(configurableObject);
    }


    @Override
    public int getClassId() {
        return ItemPortableRegistry.ASSET_CID;
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = super.toJson(super.toJson());
        return jsonObject;
    }
    @Override
    public boolean configureAndValidate(JsonObject config){
        return super.configureAndValidate(config) && _validate();
    }

    @Override
    public boolean configureAndValidate(){
        return true;
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

    private boolean _validate(){
        if(this.configurationType.equals(Configurable.ASSET_CONFIG_TYPE)) return true;
        if(this.configurationType.endsWith(".")) return false;
        String[] comp = this.configurationType.split("\\.");
        if(comp.length != 2) return false; //asset.xxx
        return comp[0].equals(Configurable.ASSET_CONFIG_TYPE);
    }

}
