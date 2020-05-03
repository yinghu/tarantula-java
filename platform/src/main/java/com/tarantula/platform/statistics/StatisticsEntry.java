package com.tarantula.platform.statistics;


import com.tarantula.Statistics;
import com.tarantula.platform.RecoverableObject;
import com.tarantula.platform.ResourceKey;
import com.tarantula.platform.presence.PresencePortableRegistry;
import com.tarantula.platform.util.SystemUtil;

import java.time.LocalDateTime;
import java.util.Map;


/**
 * Updated by yinghu lu on 4/29/2020
 */
public class StatisticsEntry extends RecoverableObject implements Statistics.Entry {

    private String name;
    private double total=0;
    private double daily=0;
    private double weekly=0;
    private double monthly=0;
    private double yearly=0;

    private boolean loaded;
    public StatisticsEntry(){
        this.vertex = "Stats";
    }
    public StatisticsEntry(String bucket,String oid,String name){
        this();
        this.bucket = bucket;
        this.oid = oid;
        this.name = name;
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
    public double monthly() {
        return monthly;
    }

    @Override
    public double yearly() {
        return yearly;
    }

    @Override
    public Statistics.Entry update(double delta) {
        total += delta;
        LocalDateTime lastUpdated = SystemUtil.fromUTCMilliseconds(timestamp);
        LocalDateTime _now = LocalDateTime.now();
        if(_now.getYear()==lastUpdated.getYear()){
            boolean _reset = _now.getDayOfYear()!=lastUpdated.getDayOfYear();
            daily = _reset?delta : (daily+delta);
            //_reset = _now.getDayOfWeek()
            weekly = _reset?delta : (weekly+delta);
            _reset = _now.getMonth()!=lastUpdated.getMonth();
            monthly = _reset?delta : (monthly+delta);
            yearly +=delta;
        }
        else{
            yearly=delta;
        }
        timestamp = SystemUtil.toUTCMilliseconds(_now);
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
        this.properties.put("monthly",monthly);
        this.properties.put("yearly",yearly);
        this.properties.put("timestamp",this.timestamp);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.total = ((Number)properties.get("total")).doubleValue();
        this.daily =((Number)properties.get("daily")).doubleValue();
        this.weekly = ((Number)properties.get("weekly")).doubleValue();
        this.monthly =((Number)properties.get("monthly")).doubleValue();
        this.yearly = ((Number)properties.get("yearly")).doubleValue();
        this.timestamp =((Number)properties.get("timestamp")).longValue();
    }
    @Override
    public Key key(){
        return new ResourceKey(this.bucket,this.oid,new String[]{vertex,name});
    }
    synchronized boolean load(){
        if(loaded){
            return false;
        }
        loaded = true;
        return this.dataStore.createIfAbsent(this,true);
    }
}
