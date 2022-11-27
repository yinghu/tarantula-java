package com.tarantula.platform.service.metrics;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.service.ServiceProvider;
import com.icodesoftware.util.RecoverableObject;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ServiceViewRequest extends RecoverableObject implements ServiceProvider.Summary{

    private static String TIME_FORMAT = "HH:mm:ss";

    private final String memberId;
    private final JsonArray metrics;

    public ServiceViewRequest(String memberId){
        this.memberId = memberId;
        this.metrics = new JsonArray();
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
        jsonObject.add("metrics",metrics);
        return jsonObject;
    }

    private void _update(String category, Object value){
        JsonObject m = new JsonObject();
        m.addProperty("category",category);
        m.addProperty("value",value.toString());
        metrics.add(m);
    }
}
