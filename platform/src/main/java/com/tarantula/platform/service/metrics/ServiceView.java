package com.tarantula.platform.service.metrics;

import com.google.gson.JsonArray;
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
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceView extends RecoverableObject implements ServiceProvider.Summary {

    private static String TIME_FORMAT = "HH:mm:ss";
    private final ConcurrentHashMap<String,FIFOBuffer<Property>> metricsMap = new ConcurrentHashMap<>();
    private final int metricsSize;
    private final int chartSize;
    private final Runnable stop;
    private final HashSet<String> categorySet;

    public ServiceView(String name,Configuration configuration, Runnable stop){
        this.name = name;
        this.metricsSize = ((Number)configuration.property("metricsSize")).intValue();
        this.chartSize = ((Number)configuration.property("chartSize")).intValue();
        this.stop = stop;
        this.categorySet = new HashSet<>();
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
    public void registerCategory(String category){
        categorySet.add(category);
    }
    public JsonObject toCategoryJson(){
        JsonObject resp = new JsonObject();
        if(categorySet.size()==0){
            resp.addProperty("successful",false);
            return resp;
        }
        resp.addProperty("successful",true);
        resp.addProperty("name",name);
        JsonArray list = new JsonArray();
        categorySet.forEach(c-> list.add(c));
        resp.add("list",list);
        return resp;
    }
    public JsonObject toMetricsJson(String category){
        JsonObject resp = new JsonObject();
        resp.addProperty("successful",true);
        resp.addProperty("name",name);
        FIFOBuffer<Property> metrics = metricsMap.get(category);
        List<Property> snapshot = metrics.list(new ArrayList<>());
        resp.add("chart",_chart(snapshot,"memberId",category));
        return resp;
    }
    public JsonObject toMetricsJson(JsonArray nodes,JsonArray categories){
        JsonArray list = new JsonArray();
        int[] ix = {0};
        categories.forEach((c)->{
            FIFOBuffer<Property> metrics = metricsMap.get(c.getAsString());
            List<Property> snapshot = metrics.list(new ArrayList<>());
            if(ix[0]<chartSize){
                list.add(_chart(snapshot,nodes.get(0).getAsString(),c.getAsString()));
            }
            ix[0]++;
        });
        JsonObject m = new JsonObject();
        m.addProperty("successful",true);
        m.addProperty("name",name);
        m.add("list",list);
        return m;
    }
    public void stop(){
        stop.run();
    }
    private JsonObject _chart(List<Property> snapshot,String memberId,String category){
        JsonObject chart = new JsonObject();
        chart.addProperty("memberId",memberId);
        chart.addProperty("label",category);
        JsonArray data = new JsonArray();
        snapshot.forEach(p->{
            JsonObject js = new JsonObject();
            js.addProperty("x",p.name());
            js.addProperty("y",p.value().toString());
            data.add(js);
        });
        chart.add("data",data);
        return chart;
    }
    private void _update(String category,Object value){
        FIFOBuffer<Property> metrics = metricsMap.computeIfAbsent(category,k-> new FIFOBuffer<>(metricsSize,new Property[metricsSize]));
        metrics.push(new DistributedProperty(LocalTime.now().format(DateTimeFormatter.ofPattern(TIME_FORMAT)),value));
    }
}
