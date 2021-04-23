package com.icodesoftware.util;

import com.icodesoftware.Consumable;
import com.icodesoftware.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConsumableObject extends RecoverableObject implements Consumable {

    protected String configurationType;
    protected String configurationName;

    public String configurationType(){return this.configurationType;}
    public void configurationType(String configurationType){
        this.configurationType = configurationType;
    }
    public String configurationName(){return this.configurationName;}
    public void configurationName(String configurationName){
        this.configurationName = configurationName;
    }

    public Map<String,Object> toMap(){
        this.properties.put("configurationType",this.configurationType);
        this.properties.put("configurationName",this.configurationName);
        return this.properties;
    }
    public void fromMap(Map<String,Object> properties){
        this.configurationType = (String) properties.remove("configurationType");
        this.configurationName = (String) properties.remove("configurationName");
        properties.forEach((String k,Object v)->this.properties.put(k,v));
    }

    @Override
    public List<Property> properties() {
        List<Property> kv = new ArrayList<>();
        properties.forEach((k,v)->{
            kv.add(new SimpleProperty(k,v));
        });
        return kv;
    }
}
