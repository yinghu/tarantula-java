package com.tarantula.platform.item;

import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;

import java.util.Map;

public class Component extends ConfigurableObject{

    public Component(){}

    public Component(ConfigurableObject configurableObject){
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
        return ItemPortableRegistry.COMPONENT_CID;
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = super.toJson();
        return jsonObject;
    }
    @Override
    public boolean configureAndValidate(JsonObject config){
        return super.configureAndValidate(config)&&this.configurationType.equals(Configurable.COMPONENT_CONFIG_TYPE);
    }
    @Override
    public  <T extends Configurable> T setup(){
        return (T)this;
    }

}
