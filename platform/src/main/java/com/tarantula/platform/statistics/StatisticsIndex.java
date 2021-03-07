package com.tarantula.platform.statistics;
import com.icodesoftware.Statistics;
import com.tarantula.platform.AssociateKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.icodesoftware.util.RecoverableObject;
/**
 * Updated by yinghu on 4/29/2020
 */
public class StatisticsIndex extends RecoverableObject implements Statistics {

    private Map<String,StatisticsEntry> mappings = new ConcurrentHashMap<>();

    public StatisticsIndex(){
        this.label = "Stats";
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
}
