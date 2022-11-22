package com.tarantula.platform.service.metrics;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Configuration;
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
    private final int chartSize;
    private final JsonArray charts;
    private final Runnable stop;

    public ServiceView(String name,Configuration configuration, Runnable stop){
        this.name = name;
        this.metricsSize = ((Number)configuration.property("metricsSize")).intValue();
        this.chartSize = ((Number)configuration.property("chartSize")).intValue();
        this.charts = ((JsonElement)configuration.property("charts")).getAsJsonArray();
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

    public JsonObject toJson(){
        JsonArray list = new JsonArray();
        int[] ix = {0};
        metricsMap.forEach((k,v)->{
            FIFOBuffer<Property> metrics = v;
            List<Property> snapshot = metrics.list(new ArrayList<>());
            JsonObject chart = new JsonObject();
            if(ix[0]<chartSize){
                JsonObject ref = charts.get(ix[0]).getAsJsonObject();
                chart.addProperty("label",k);
                chart.addProperty("backgroundColor",ref.get("backgroundColor").getAsString());
                chart.addProperty("borderColor",ref.get("borderColor").getAsString());
                chart.addProperty("cubicInterpolationMode",ref.get("cubicInterpolationMode").getAsString());
                chart.addProperty("borderWidth",ref.get("borderWidth").getAsInt());
                chart.addProperty("pointBorderWidth",ref.get("pointBorderWidth").getAsInt());
                chart.addProperty("pointRadius",ref.get("pointRadius").getAsInt());
                JsonArray data = new JsonArray();
                snapshot.forEach(p->{
                    JsonObject js = new JsonObject();
                    js.addProperty("x",p.name());
                    js.addProperty("y",p.value().toString());
                    data.add(js);
                });
                chart.add("data",data);
                list.add(chart);
            }
            ix[0]++;
        });
        JsonObject m = new JsonObject();
        m.add("metrics",list);
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
