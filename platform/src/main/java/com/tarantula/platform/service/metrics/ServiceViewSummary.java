package com.tarantula.platform.service.metrics;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Configuration;
import com.icodesoftware.service.ServiceProvider;
import com.icodesoftware.util.RecoverableObject;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceViewSummary extends RecoverableObject implements ServiceProvider.Summary {


    private final int metricsSize;
    private final int chartSize;
    private final Runnable stop;
    private final HashSet<String> categorySet;

    private final ConcurrentHashMap<String, ServiceView> viewMap = new ConcurrentHashMap<>();

    public ServiceViewSummary(String name, Configuration configuration, Runnable stop){
        this.name = name;
        this.metricsSize = ((Number)configuration.property("metricsSize")).intValue();
        this.chartSize = ((Number)configuration.property("chartSize")).intValue();
        this.stop = stop;
        this.categorySet = new HashSet<>();
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
    public JsonObject toMetricsJson(JsonArray nodes,JsonArray categories){
        JsonObject m = new JsonObject();
        if(nodes.size()==0 || categories.size()==0){
            m.addProperty("successful",false);
            m.addProperty("message","at one node or category");
            return m;
        }
        JsonArray list = new JsonArray();
        nodes.forEach((n)->{
            ServiceView view = viewMap.get(n.getAsString());
            if(view!=null){
                list.add(view.toMetricsJson(categories));
            }
        });
        m.addProperty("name",name);
        m.addProperty("successful",true);
        m.add("list",list);
        return m;
    }
    public void stop(){
        stop.run();
    }

    public void update(JsonObject payload){
        String memberId = payload.get("memberId").getAsString();
        String timed = payload.get("time").getAsString();
        viewMap.compute(memberId,(k,v)->{
            if(v==null) v = new ServiceView(memberId,metricsSize,chartSize);
            JsonArray data = payload.get("metrics").getAsJsonArray();
            for(JsonElement j : data) {
                JsonObject m = j.getAsJsonObject();
                String category = m.getAsJsonObject().get("category").getAsString();
                String value = m.getAsJsonObject().get("value").getAsString();
                v.update(category, timed, value);
            }
            return v;
        });
    }
}
