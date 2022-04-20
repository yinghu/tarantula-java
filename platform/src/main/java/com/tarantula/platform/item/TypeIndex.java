package com.tarantula.platform.item;

import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.NaturalKey;
import com.icodesoftware.util.RecoverableObject;

import java.util.Map;

public class TypeIndex extends RecoverableObject implements Configurable {


    public JsonObject payload = new JsonObject();

    public TypeIndex(String name,String index,String label){
        this.name = name;
        this.index = index;
        this.label = label;
    }

    @Override
    public Map<String,Object> toMap(){
        properties.put("index",index);
        properties.put("type",label);
        properties.put("payload",payload.toString());
        return properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.index = (String) properties.get("index");
        this.label = (String) properties.get("label");
        this.payload = JsonUtil.parse((String) properties.getOrDefault("payload","{}"));
    }

    public Key key(){
        return new NaturalKey(name);
    }
}
