package com.tarantula.admin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.tarantula.AccessIndex;

import java.lang.reflect.Type;


public class AdminUserObject extends AdminObject {

    private AccessIndex accessIndex;

    public AdminUserObject(String message,String label){
        super(label);
        this.message = message;
    }
    public AdminUserObject(AccessIndex accessIndex,String label){
        super(label);
        this.accessIndex = accessIndex;
    }
    public JsonElement setup(Type type, JsonSerializationContext jsonSerializationContext){
        JsonObject jo = super.setup(type,jsonSerializationContext).getAsJsonObject();
        if(accessIndex!=null){
            jo.addProperty("systemId",accessIndex.distributionKey());
        }

        return jo;
    }

}
