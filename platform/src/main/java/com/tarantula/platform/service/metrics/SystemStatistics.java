package com.tarantula.platform.service.metrics;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Statistics;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.statistics.StatisticsPortableRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class SystemStatistics extends RecoverableObject implements Statistics {


    private final static String LABEL = "statistics";

    private Map<String, SystemStatisticsEntry> mappings = new ConcurrentHashMap<>();

    public SystemStatistics(){
        this.label = LABEL;
    }

    public Entry entry(String key) {
        SystemStatisticsEntry entry = this.mappings.computeIfAbsent(key,(k)->{
            //new entry
            SystemStatisticsEntry se = new SystemStatisticsEntry(this.bucket,this.oid,k);
            se.dataStore(this.dataStore);
            return se;
        });
        if(entry.load()){
            this.dataStore.update(this);//update index
        }//load as request
        return entry;
    }
    //memory copy list
    public List<Entry> summary(){
        ArrayList<Entry> elist = new ArrayList<>();
        mappings.forEach((k,v)->{
            elist.add(v.duplicate());
        });
        return elist;
    }
    //original streaming
    public void summary(Stream query){
        mappings.forEach((k,v)->{
            query.onEntry(v);
        });
    }
    @Override
    public int getFactoryId() {
        return StatisticsPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return StatisticsPortableRegistry.SYSTEM_STATISTICS_CID;
    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.clear();
        this.mappings.forEach((k,v)->{
            this.properties.put(v.name(),"1");//index stats name
        });
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        properties.forEach((k,v)->{
            SystemStatisticsEntry entry = new SystemStatisticsEntry(this.bucket,this.oid,k);
            entry.dataStore(this.dataStore);
            //entry.load();
            mappings.put(k,entry);
        });
    }
    @Override
    public Key key(){
        return new AssociateKey(this.bucket,this.oid,this.label);
    }

    @Override
    public JsonObject toJson() {
        JsonObject jo  = new JsonObject();
        JsonArray ja = new JsonArray();
        for(Entry entry : summary()){
            ja.add(entry.toJson());
        }
        jo.addProperty("Successful",true);
        jo.add("_categories",ja);
        return jo;
    }
}
