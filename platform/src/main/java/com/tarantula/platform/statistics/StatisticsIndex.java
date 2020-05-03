package com.tarantula.platform.statistics;
import com.tarantula.*;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.RecoverableObject;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Updated by yinghu on 4/29/2020
 */
public class StatisticsIndex extends RecoverableObject implements Statistics {

    private Map<String,StatisticsEntry> mappings = new ConcurrentHashMap<>();

    public StatisticsIndex(){
        this.vertex = "Statistics";
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
    //heavy operations should be avoided
    public List<Entry> summary(){
        ArrayList<Entry> elist = new ArrayList<>();
        mappings.forEach((k,v)->{
            v.load();
            elist.add(v);
        });
        return elist;
    }
    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PresencePortableRegistry.STATISTICS_CID;
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
        return new AssociateKey(this.bucket,this.oid,this.vertex);
    }
}
