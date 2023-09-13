package com.tarantula.platform.service.metrics;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Statistics;
import com.icodesoftware.util.RecoverableObject;

import com.tarantula.platform.statistics.StatisticsPortableRegistry;
import com.tarantula.platform.util.RecoverableQuery;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class SystemStatistics extends RecoverableObject implements Statistics {



    private Map<String, SystemStatisticsEntry> mappings = new ConcurrentHashMap<>();

    public SystemStatistics(){

    }


    public Entry entry(String key) {
        return this.mappings.computeIfAbsent(key,(k)->{
            //new entry
            SystemStatisticsEntry se = new SystemStatisticsEntry(k,this.label);
            se.ownerKey(this.key());
            this.dataStore.create(se);
            se.dataStore(this.dataStore);
            return se;
        });
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

    public void load(){
        RecoverableQuery<SystemStatisticsEntry> query = RecoverableQuery.query(this.distributionId,new SystemStatisticsEntry("",label),StatisticsPortableRegistry.INS);
        dataStore.list(query, e->{
            mappings.put(e.name(),e);
            return true;
        });
    }
    public MetricsHistory loadMetricsHistory(int day){
        RecoverableQuery<MetricsHistory> query = RecoverableQuery.query(distributionId(),new MetricsHistory(SystemMetrics.ACCESS_AMAZON_S3_COUNT, LocalDateTime.now().getYear(),1), StatisticsPortableRegistry.INS);
        MetricsHistory[] loaded ={null};
        dataStore.list(query,m->{
            if(m.day==day){
                loaded[0]=m;
                return false;
            }
            return true;
        });
        return loaded[0];
    }
}
