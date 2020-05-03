package com.tarantula.platform.statistics;


import com.tarantula.Statistics;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.RecoverableObject;
import com.tarantula.platform.ResourceKey;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.util.Map;


/**
 * Updated by yinghu lu on 4/29/2020
 */
public class StatisticsEntry extends RecoverableObject implements Statistics.Entry {

    private String name;
    private double total=0;
    private double daily;
    private double weekly;

    public StatisticsEntry(){
        this.vertex = "Stats";
    }
    public StatisticsEntry(String name){
        this();
        this.name = name;
    }
    public StatisticsEntry(String name,double value){
        this(name);
        this.total = value;
    }
    @Override
    public String name() {
        return name;
    }
    @Override
    public double total() {
        return this.total;
    }

    @Override
    public double daily() {
        return daily;
    }

    @Override
    public double weekly() {
        return weekly;
    }

    @Override
    public Statistics.Entry update(double value) {
        return this;
    }
    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PresencePortableRegistry.STATISTICS_ENTRY_CID;
    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("total",total);
        this.properties.put("daily",daily);
        this.properties.put("weekly",weekly);
        this.properties.put("timestamp",this.timestamp);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        
    }
    @Override
    public Key key(){
        return new ResourceKey(this.bucket,this.oid,new String[]{vertex,name});
    }

}
