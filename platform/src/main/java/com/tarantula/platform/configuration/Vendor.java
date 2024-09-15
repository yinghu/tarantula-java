package com.tarantula.platform.configuration;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class Vendor {
    private final JsonObject payload;
    public Vendor(JsonObject payload){
        this.payload = payload;
    }
    public String name(){
        return payload.get("name").getAsString();
    }
    public boolean disabled(){
        return payload.get("disabled").getAsBoolean();
    }
    public String configuration(){
        return payload.get("configuration").getAsString();
    }

    public String packageName(){
        return payload.get("package").getAsString();
    }
    public List<String> providers(){
        ArrayList<String> list = new ArrayList<>();
        payload.get("providers").getAsJsonArray().forEach(e->{
            list.add(e.getAsString());
        });
        return list;
    }
    public CredentialConfiguration credentialConfiguration(String typeId,JsonObject payload){
        try{
            return (CredentialConfiguration)Class.forName(packageName()+"."+configuration()).getConstructor(String.class,JsonObject.class).newInstance(typeId,payload);
        }catch (Exception ex){
            return null;
        }
    }
}
