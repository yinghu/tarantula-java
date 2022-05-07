package com.tarantula.platform.item;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;

import java.util.ArrayList;
import java.util.Map;

public class Application extends ConfigurableObject implements Configurable.Listener<Commodity>{

    protected boolean validated;

    public Application(){}

    public Application(ConfigurableObject configurableObject){
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
        return ItemPortableRegistry.APPLICATION_CID;
    }
    @Override
    public JsonObject toJson(){
        JsonObject json = super.toJson();
        _reference.forEach((cob)->{
            json.add(cob.distributionKey(),cob.toJson());
        });
        return json;
    }

    @Override
    public boolean configureAndValidate(JsonObject config){
        return super.configureAndValidate(config)&&this.configurationType.startsWith(Configurable.APPLICATION_CONFIG_TYPE);
    }

    @Override
    public boolean configureAndValidate(){
        setup();
        return validated;
        //int passed = 0;//have to have at least one of references item, commodity, component or asset
        //for(JsonElement je : this.reference){
            //ConfigurableObject cob = new ConfigurableObject();
            //cob.distributionKey(je.getAsString());
            //if(dataStore.load(cob) && !cob.configurationType().equals(Configurable.APPLICATION_CONFIG_TYPE)){
                //passed++;
            //}
            //else{
                //passed=0;//invalid reference
                //break;
            //}
        //}
        //return passed>0;
    }
    @Override
    public  <T extends Configurable> T setup(){
        _reference = new ArrayList<>();
        for(JsonElement je : reference){
            ConfigurableObject cob = new ConfigurableObject();
            cob.distributionKey(je.getAsString());
            cob.dataStore(dataStore);
            if(this.dataStore.load(cob) && !cob.configurationType.equals(Configurable.APPLICATION_CONFIG_TYPE)){
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
}
