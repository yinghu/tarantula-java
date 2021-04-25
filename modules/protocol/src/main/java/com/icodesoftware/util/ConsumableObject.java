package com.icodesoftware.util;

import com.icodesoftware.Consumable;
import com.icodesoftware.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConsumableObject extends RecoverableObject implements Consumable {

    protected String configurationType;
    protected String configurationName;
    protected String configurationCategory;

    public String configurationType(){return this.configurationType;}
    public void configurationType(String configurationType){
        this.configurationType = configurationType;
    }
    public String configurationName(){return this.configurationName;}
    public void configurationName(String configurationName){
        this.configurationName = configurationName;
    }
    public String configurationCategory(){return this.configurationCategory;}
    public void configurationCategory(String configurationCategory){ this.configurationCategory = configurationCategory; }

    public Map<String,Object> toMap(){
        this.properties.put("configurationType",this.configurationType);
        this.properties.put("configurationName",this.configurationName);
        this.properties.put("configurationCategory",this.configurationCategory);
        this.properties.put("disabled",disabled);
        return this.properties;
    }
    public void fromMap(Map<String,Object> properties){
        this.configurationType = (String) properties.remove("configurationType");
        this.configurationName = (String) properties.remove("configurationName");
        this.configurationCategory = (String) properties.remove("configurationCategory");
        this.disabled = (boolean)properties.remove("disabled");
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
