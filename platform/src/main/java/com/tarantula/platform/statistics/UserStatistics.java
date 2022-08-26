package com.tarantula.platform.statistics;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Statistics;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.AssociateKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class UserStatistics extends RecoverableObject implements Statistics {

    private Map<String,StatisticsEntry> mappings = new ConcurrentHashMap<>();
    private Listener listener;
    public UserStatistics(){
        this.label = "Stats";
    }

    public void registerListener(Listener listener){
        this.listener = listener;
    }

    public Entry entry(String key) {
        StatisticsEntry entry = this.mappings.computeIfAbsent(key,(k)->{
            //new entry
            StatisticsEntry se = new StatisticsEntry(this.bucket,this.oid,k);
            se.dataStore(this.dataStore);
            return se;
        });
        if(entry.load()){
            this.dataStore.update(this);//update index
        }//load as request
        entry.listener(this.listener);
        return entry;
    }
    //memory copy list
    public List<Entry> summary(){
        ArrayList<Entry> elist = new ArrayList<>();
        mappings.forEach((k,v)->{
            v.load();
            elist.add(v.duplicate());
        });
        return elist;
    }
    //original streaming
    public void summary(Stream query){
        mappings.forEach((k,v)->{
            v.load();
            query.onEntry(v);
        });
    }
    @Override
    public int getFactoryId() {
        return StatisticsPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return StatisticsPortableRegistry.STATISTICS_CID;
    }

    @Override
    public Map<String,Object> toMap(){
        this.mappings.forEach((k,v)->{
            this.properties.put(v.name(),"");//index stats name
        });
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        properties.forEach((k,v)->{
            StatisticsEntry entry = new StatisticsEntry(this.bucket,this.oid,k);
            entry.dataStore(this.dataStore);
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
        for(Statistics.Entry entry : summary()){
            ja.add(entry.toJson());
        }
        jo.addProperty("Successful",true);
        jo.add("_categories",ja);
        return jo;
    }
}
