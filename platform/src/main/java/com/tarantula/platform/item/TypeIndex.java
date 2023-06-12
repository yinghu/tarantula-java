package com.tarantula.platform.item;

import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.NaturalKey;
import com.icodesoftware.util.RecoverableObject;

import java.util.Map;

public class TypeIndex extends RecoverableObject implements Configurable {

    public enum Typed {Primitive,Enum,Category};

    private JsonObject history = new JsonObject();
    private JsonObject payload = new JsonObject();
    public Typed typed;

    public TypeIndex(String name){
        this.name = name;
    }
    public TypeIndex(String name,Typed typed,String scope,JsonObject payload){
        this(name);
        this.typed = typed;
        this.index = scope;
        this.upgrade(payload);
    }

    @Override
    public Map<String,Object> toMap(){
        properties.put("scope",index);
        properties.put("typed",typed.name());
        properties.put("history",history.toString());
        properties.put("payload",payload.toString());
        return properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.index = (String)properties.get("scope");
        this.typed = Typed.valueOf((String)properties.get("typed"));
        this.history = JsonUtil.parse((String) properties.getOrDefault("history","{}"));
        this.payload = JsonUtil.parse((String) properties.getOrDefault("payload","{}"));
    }

    public void upgrade(JsonObject latest){
        this.history = payload;
        this.payload = latest;
    }

    public JsonObject payload(){
        return payload;
    }
    public JsonObject history(){
        return history;
    }

    public Key key(){
        return new NaturalKey(name);
    }

}
