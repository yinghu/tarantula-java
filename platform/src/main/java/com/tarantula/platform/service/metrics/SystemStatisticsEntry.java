package com.tarantula.platform.service.metrics;

import com.google.gson.JsonObject;
import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.Statistics;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.ResourceKey;
import com.tarantula.platform.statistics.StatisticsPortableRegistry;
import com.tarantula.platform.statistics.StatisticsUtil;

import java.time.LocalDateTime;
import java.util.Map;

public class SystemStatisticsEntry extends RecoverableObject implements Statistics.Entry {

    private final static String LABEL = "category";
    //private String name;
    private double total=0;
    private double hourly=0;
    private double daily=0;
    private double weekly=0;
    private double monthly=0;
    private double yearly=0;

    private boolean loaded;

    public SystemStatisticsEntry(){
        this.label = LABEL;
    }
    public SystemStatisticsEntry(String bucket, String oid, String name){
        this();
        this.bucket = bucket;
        //this.oid = oid;
        this.name = name;
    }
    public SystemStatisticsEntry(Statistics.Entry entry){
        this();
        this.name = entry.name();
        this.hourly = entry.hourly();
        this.daily = entry.daily();
        this.weekly = entry.weekly();
        this.monthly = entry.monthly();
        this.yearly = entry.yearly();
        this.total = entry.total();
    }

    @Override
    public int scope() {
        return Distributable.LOCAL_SCOPE;
    }

    @Override
    public String name() {
        return name;
    }
    @Override
    public double total() {
        return this.total;
    }
    void total(double total,LocalDateTime update){
        this.total = total;
        this.timestamp = TimeUtil.toUTCMilliseconds(update);
    }
    @Override
    public double hourly() {
        return hourly;
    }
    void hourly(double hourly,LocalDateTime update){
        this.hourly = hourly;
        this.timestamp = TimeUtil.toUTCMilliseconds(update);
    }
    @Override
    public double daily() {
        return daily;
    }

    void daily(double daily,LocalDateTime update){
        this.daily = daily;
        this.timestamp = TimeUtil.toUTCMilliseconds(update);
    }
    @Override
    public double weekly() {
        return weekly;
    }

    void weekly(double weekly,LocalDateTime update){
        this.weekly = weekly;
        this.timestamp = TimeUtil.toUTCMilliseconds(update);
    }
    @Override
    public double monthly() {
        return monthly;
    }

    void monthly(double monthly,LocalDateTime update){
        this.monthly = monthly;
        this.timestamp = TimeUtil.toUTCMilliseconds(update);
    }
    @Override
    public double yearly() {
        return yearly;
    }

    void yearly(double yearly,LocalDateTime update){
        this.yearly = yearly;
        this.timestamp = TimeUtil.toUTCMilliseconds(update);
    }
    @Override
    public synchronized Statistics.Entry update(double delta) {
        LocalDateTime _now = LocalDateTime.now();
        hourly += delta;
        daily += delta;
        weekly += delta;
        monthly += delta;
        yearly += delta;
        total += delta;
        timestamp = TimeUtil.toUTCMilliseconds(_now);
        return this;
    }
    @Override
    public int getFactoryId() {
        return StatisticsPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return StatisticsPortableRegistry.SYSTEM_STATISTICS_ENTRY_CID;
    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("total",total);
        this.properties.put("hourly",hourly);
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
        this.hourly =((Number)properties.get("hourly")).doubleValue();
        this.daily =((Number)properties.get("daily")).doubleValue();
        this.weekly = ((Number)properties.get("weekly")).doubleValue();
        this.monthly =((Number)properties.get("monthly")).doubleValue();
        this.yearly = ((Number)properties.get("yearly")).doubleValue();
        this.timestamp =((Number)properties.get("timestamp")).longValue();
    }


    Statistics.Entry duplicate(){
        return new SystemStatisticsEntry(this);
    }
    public synchronized boolean load(){
        if(loaded){
            return false;
        }
        loaded = true;
        this.dataStore.createIfAbsent(this,true);
        return true;
    }
}
