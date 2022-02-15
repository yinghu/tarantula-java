package com.tarantula.platform.store;

import com.icodesoftware.util.RecoverableObject;

import java.util.Map;

public class Transaction extends RecoverableObject{

    public String originalPayload;
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("originalPayload",originalPayload);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.originalPayload = (String) properties.getOrDefault("originalPayload","{}");
    }
}
