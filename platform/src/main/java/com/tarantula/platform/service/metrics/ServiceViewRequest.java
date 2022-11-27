package com.tarantula.platform.service.metrics;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.service.ServiceProvider;
import com.icodesoftware.util.RecoverableObject;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class ServiceViewRequest extends RecoverableObject implements ServiceProvider.Summary{

    private static String TIME_FORMAT = "HH:mm:ss";

    private final String memberId;
    private final HashMap<String,Object> pendingUpdates;

    public ServiceViewRequest(String memberId, String[] categories){
        this.memberId = memberId;
        this.pendingUpdates = new HashMap<>();
        for(String c : categories){
            pendingUpdates.put(c,0);
        }
    }

    @Override
    public void update(String category, int value) {
        _update(category,value);
    }

    @Override
    public void update(String category, long value) {
        _update(category,value);
    }

    @Override
    public void update(String category, double value) {
        _update(category,value);
    }

    @Override
    public void registerCategory(String category) {

    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("memberId",memberId);
        jsonObject.addProperty("time",LocalTime.now().format(DateTimeFormatter.ofPattern(TIME_FORMAT)));
        JsonArray data = new JsonArray();
        pendingUpdates.forEach((k,v)->{
            JsonObject m = new JsonObject();
            m.addProperty("category",k);
            m.addProperty("value",v.toString());
            data.add(m);
        });
        jsonObject.add("metrics",data);
        return jsonObject;
    }

    private void _update(String category, Object value){
        if(!pendingUpdates.containsKey(category)) return;
        pendingUpdates.replace(category,value);
    }
}
