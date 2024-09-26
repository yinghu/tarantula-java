package com.tarantula.platform.item;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;

import java.util.ArrayList;
import java.util.List;

public class Application extends ConfigurableObject implements Configurable.Listener<Commodity>{

    protected boolean validated;
    public int configurationId;
    public int publishId;

    public Application(){}

    public Application(JsonObject payload){
        this.header = payload;
        this.configurationId = payload.get("ConfigurationId").getAsInt();
        this.configurationName = payload.get("ConfigurationName").getAsString();
        if(payload.has("ConfigurationPublishId")) this.publishId = payload.get("ConfigurationPublishId").getAsInt();
    }

    public Application(ConfigurableObject configurableObject){
        super(configurableObject);
    }

    @Override
    public int getClassId() {
        return ItemPortableRegistry.APPLICATION_CID;
    }
    @Override
    public JsonObject toJson(){
        JsonObject jso = super.toJson(super.toJson());
        jso.addProperty("Successful",true);
        return jso;
    }

    @Override
    public boolean configureAndValidate(JsonObject config) {
        return super.configureAndValidate(config) && _validate();
    }
    @Override
    public boolean configureAndValidate(){
        if(configurationScope.endsWith(".data") || configurationScope.endsWith(".lobby")) return true;
        setup();
        return validated;
    }
    @Override
    public  <T extends Configurable> T setup(){
        _reference = new ArrayList<>();
        for(JsonElement je : reference){
            ConfigurableObject cob = new ConfigurableObject();
            cob.distributionId(je.getAsLong());
            cob.dataStore(dataStore);
            if(this.dataStore.load(cob)){
                cob.registerListener(this);
                _reference.add(cob.setup());
            }
            else{
                validated = false;
                break;
            }
        }
        return (T)this;
    }
    @Override
    public void onLoaded(Commodity commodity){
        this.validated = true;
    }

    private boolean _validate(){
        if(this.configurationType.equals(Configurable.APPLICATION_CONFIG_TYPE)) return true;
        if(this.configurationType.endsWith(".")) return false;
        String[] comp = this.configurationType.split("\\.");
        if(comp.length != 2) return false; //asset.xxx
        return comp[0].equals(Configurable.APPLICATION_CONFIG_TYPE);
    }

    public List<Commodity> commodityList(){
        return new ArrayList<>();
    }

    public String configurationKey(){
        return Integer.toString(configurationId);
    }
    public String publishKey(){
        return Integer.toString(publishId);
    }
}
