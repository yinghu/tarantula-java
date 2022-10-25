package com.tarantula.platform.service.metrics;

import com.google.gson.JsonObject;
import com.icodesoftware.service.ServiceProvider;
import com.icodesoftware.util.RecoverableObject;

public class ServiceView extends RecoverableObject implements ServiceProvider.Summary {

    @Override
    public void update(String category, int value) {
        properties.put(category,value);
    }

    @Override
    public void update(String category, long value) {
        properties.put(category,value);
    }

    @Override
    public void update(String category, double value) {
        properties.put(category,value);
    }

    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        properties.forEach((k,v)-> jsonObject.addProperty(k,(Number)v));
        return jsonObject;
    }
}
