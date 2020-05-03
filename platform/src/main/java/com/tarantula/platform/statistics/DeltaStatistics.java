package com.tarantula.platform.statistics;
import com.tarantula.*;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.RecoverableObject;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Updated by yinghu on 4/29/2020
 */
public class DeltaStatistics extends RecoverableObject implements Statistics {

    private Map<String,Entry> mappings = new ConcurrentHashMap<>();

    public DeltaStatistics(){
        this.vertex = "Statistics";
    }

    public Entry entry(String key) {
        return this.mappings.computeIfAbsent(key,(k)->new StatisticsEntry(k));
    }
    public Map<String,Double> summary(){
        Map<String,Double> _mv = new HashMap<>();
        this.mappings.forEach((k,v)->{
            _mv.put(k,v.total());
        });
        return _mv;
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
            this.properties.put(v.name(),v.total());
        });
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        properties.forEach((k,v)->{
            double vo = ((Number)v).doubleValue();
            mappings.put(k,new StatisticsEntry(k,vo));
        });
    }
    @Override
    public Key key(){
        return new AssociateKey(this.bucket,this.oid,this.vertex);
    }
}
