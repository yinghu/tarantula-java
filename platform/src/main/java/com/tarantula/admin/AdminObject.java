package com.tarantula.admin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.tarantula.OnApplication;
import com.tarantula.platform.OnApplicationHeader;

import java.lang.reflect.Type;


public class AdminObject extends OnApplicationHeader implements OnApplication {


    public AdminObject(String label){
        this.label = label;
        this.successful = true;
    }
    public JsonElement setup(Type type, JsonSerializationContext jsonSerializationContext){
        return new JsonObject();
    }

    @Override
    public int getFactoryId() {
        return 0;
    }

    @Override
    public int getClassId() {
        return 0;
    }
}
