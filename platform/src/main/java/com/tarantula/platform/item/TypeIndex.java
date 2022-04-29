package com.tarantula.platform.item;

import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.NaturalKey;
import com.icodesoftware.util.RecoverableObject;

import java.util.Map;

public class TypeIndex extends RecoverableObject implements Configurable {


    public JsonObject payload = new JsonObject();

    public TypeIndex(String name){
        this.name = name;
    }
    public TypeIndex(String name,String scope,JsonObject payload){
        this(name);
        this.index = scope;
        this.payload = payload;
    }

    @Override
    public Map<String,Object> toMap(){
        properties.put("scope",index);
        properties.put("payload",payload.toString());
        return properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.index = (String)properties.get("scope");
        this.payload = JsonUtil.parse((String) properties.getOrDefault("payload","{}"));
    }

    public Key key(){
        return new NaturalKey(name);
    }
}
