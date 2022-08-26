package com.tarantula.platform.statistics;

import com.google.gson.JsonObject;
import com.icodesoftware.Recoverable;
import com.icodesoftware.Statistics;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;

import com.tarantula.platform.ResourceKey;

import java.time.LocalDateTime;
import java.time.temporal.IsoFields;
import java.util.Map;

public class StatisticsEntry extends RecoverableObject implements Statistics.Entry {

    private String name;
    private double total=0;
    private double daily=0;
    private double weekly=0;
    private double monthly=0;
    private double yearly=0;

    private boolean loaded;
    private Statistics.Listener listener;
    public StatisticsEntry(){
        this.label = "Stats";
    }
    public StatisticsEntry(String bucket,String oid,String name){
        this();
        this.bucket = bucket;
        this.oid = oid;
        this.name = name;
    }
    public StatisticsEntry(Statistics.Entry entry){
        this.name = entry.name();
        this.daily = entry.daily();
        this.weekly = entry.weekly();
        this.monthly = entry.monthly();
        this.yearly = entry.yearly();
        this.total = entry.total();
    }
    public void listener(Statistics.Listener listener){
        this.listener = listener;
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
    public synchronized Statistics.Entry update(double delta) {
        LocalDateTime lastUpdated = TimeUtil.fromUTCMilliseconds(timestamp);
        LocalDateTime _now = LocalDateTime.now();
        if(_now.getYear()==lastUpdated.getYear()){//check in same year
            if(_now.getDayOfYear()==lastUpdated.getDayOfYear()){//same day update
                daily +=delta;
                weekly +=delta;
                monthly +=delta;
            }//another day
            else{
                daily = delta;
                weekly = (_now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)==lastUpdated.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR))?(weekly+delta):delta;
                monthly = (_now.getMonth().getValue()==lastUpdated.getMonth().getValue())?(monthly+delta):delta;
            }
            yearly +=delta;
        }
        else{//reset on another year include week
            daily = delta;
            weekly = delta;
            monthly = delta;
            yearly = delta;
        }
        total += delta;
        timestamp = TimeUtil.toUTCMilliseconds(_now);
        if(listener!=null){
            listener.entryUpdated(this);
        }
        return this;
    }
    @Override
    public int getFactoryId() {
        return StatisticsPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return StatisticsPortableRegistry.STATISTICS_ENTRY_CID;
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
    public void distributionKey(String distributionKey){
        //parse key
        String[] k = distributionKey.split(Recoverable.PATH_SEPARATOR);
        bucket = k[0];
        oid = k[1];
        name = k[3];
    }
    @Override
    public Key key(){
        return new ResourceKey(this.bucket,this.oid,new String[]{label,name});
    }
    Statistics.Entry duplicate(){
        return new StatisticsEntry(this);
    }
    public synchronized boolean load(){
        if(loaded){
            return false;
        }
        loaded = true;
        return this.dataStore.createIfAbsent(this,true);
    }
    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Name",name);
        jsonObject.addProperty("Daily",daily);
        jsonObject.addProperty("Weekly",weekly);
        jsonObject.addProperty("Monthly",monthly);
        jsonObject.addProperty("Yearly",yearly);
        jsonObject.addProperty("Total",total);
        return jsonObject;
    }
}
