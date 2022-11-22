package com.tarantula.platform.service.metrics;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Property;
import com.icodesoftware.service.ServiceProvider;
import com.icodesoftware.util.FIFOBuffer;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.DistributedProperty;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceView extends RecoverableObject implements ServiceProvider.Summary {

    private static String TIME_FORMAT = "HH:mm:ss";
    private final ConcurrentHashMap<String,FIFOBuffer<Property>> metricsMap = new ConcurrentHashMap<>();
    private final int metricsSize;
    private final Runnable stop;

    public ServiceView(String name,int size,Runnable stop){
        this.name = name;
        this.metricsSize = size;
        this.stop = stop;
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

    public JsonObject metrics(String category){
        FIFOBuffer<Property> metrics = metricsMap.get(category);
        if(metrics==null) return new JsonObject();
        List<Property> data = metrics.list(new ArrayList<>());
        JsonObject m = new JsonObject();
        JsonArray ms = new JsonArray();
        data.forEach(p->{
            JsonObject js = new JsonObject();
            js.addProperty("x",p.name());
            js.addProperty("y",p.value().toString());
            ms.add(js);
        });
        m.add("metrics",ms);
        return m;
    }
    public void stop(){
        stop.run();
    }
    private void _update(String category,Object value){
        FIFOBuffer<Property> metrics = metricsMap.computeIfAbsent(category,k-> new FIFOBuffer<>(metricsSize,new Property[metricsSize]));
        metrics.push(new DistributedProperty(LocalTime.now().format(DateTimeFormatter.ofPattern(TIME_FORMAT)),value));
    }
}
