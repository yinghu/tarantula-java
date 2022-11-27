package com.tarantula.platform.service.metrics;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Property;
import com.icodesoftware.util.FIFOBuffer;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.DistributedProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceView extends RecoverableObject{

    private final ConcurrentHashMap<String,FIFOBuffer<Property>> metricsMap = new ConcurrentHashMap<>();
    private final int metricsSize;
    private final int chartSize;

    public ServiceView(String name,int metricsSize,int chartSize){
        this.name = name;//memberId
        this.metricsSize = metricsSize;
        this.chartSize = chartSize;
    }


    public JsonObject toMetricsJson(String category){
        JsonObject resp = new JsonObject();
        resp.addProperty("memberId",name);
        FIFOBuffer<Property> metrics = metricsMap.get(category);
        List<Property> snapshot = metrics.list(new ArrayList<>());
        resp.add("chart",_chart(snapshot,category));
        return resp;
    }
    public JsonObject toMetricsJson(JsonArray categories){
        JsonArray list = new JsonArray();
        int[] ix = {0};
        categories.forEach((c)->{
            FIFOBuffer<Property> metrics = metricsMap.get(c.getAsString());
            List<Property> snapshot = metrics.list(new ArrayList<>());
            if(ix[0]<chartSize){
                list.add(_chart(snapshot,c.getAsString()));
            }
            ix[0]++;
        });
        JsonObject m = new JsonObject();
        m.addProperty("memberId",name);
        m.add("metrics",list);
        return m;
    }

    private JsonObject _chart(List<Property> snapshot,String category){
        JsonObject chart = new JsonObject();
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
    public void update(String category,String timed, Object value){
        FIFOBuffer<Property> metrics = metricsMap.computeIfAbsent(category,k-> new FIFOBuffer<>(metricsSize,new Property[metricsSize]));
        metrics.push(new DistributedProperty(timed,value));
    }
}
