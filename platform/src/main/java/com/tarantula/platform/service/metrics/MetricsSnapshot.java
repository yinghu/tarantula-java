package com.tarantula.platform.service.metrics;

import com.google.gson.JsonObject;
import com.icodesoftware.Property;
import com.icodesoftware.Recoverable;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.ResourceKey;
import com.tarantula.platform.statistics.StatisticsPortableRegistry;

import java.time.LocalDateTime;
import java.util.Map;

public class MetricsSnapshot extends RecoverableObject  {

    private Property[] metrics;
    private int trackingNumber;

    public MetricsSnapshot(int trackingNumber,String category,String classifier){
        this.trackingNumber = trackingNumber;
        this.name = category;
        this.index = classifier;
        this.metrics = new Property[trackingNumber];
    }

    public MetricsSnapshot(){

    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("trackingNumber",trackingNumber);
        this.properties.put("timestamp",timestamp);
        for(int i=0;i<trackingNumber;i++){
            this.properties.put("m"+i,metrics[i].toJson().toString());
        }
        return this.properties;
    }

    @Override
    public void fromMap(Map<String,Object> properties){
        this.trackingNumber = ((Number)properties.get("trackingNumber")).intValue();
        this.timestamp = ((Number)properties.get("timestamp")).longValue();
        this.metrics = new Property[trackingNumber];
        for(int i=0;i<trackingNumber;i++){
            JsonObject mj = JsonUtil.parse((String)properties.get("m"+i));
            metrics[i] = new MetricsProperty(i,mj.get("name").getAsString(),mj.get("value").getAsString(),mj.get("timestamp").getAsLong());
        }
    }


    public Property[] metrics(){
        return metrics;
    }
    public void distributionKey(String rkey){
        String[] idx = rkey.split(Recoverable.PATH_SEPARATOR);
        bucket = idx[0];
        oid = idx[1];
        index = idx[2];
        name = idx[3];
    }
    @Override
    public int getFactoryId() {
        return StatisticsPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return StatisticsPortableRegistry.METRICS_SNAPSHOT_CID;
    }

    public Key key(){
        return new ResourceKey(this.bucket,oid,new String[]{index,name});
    }

    public void initialize(Property property,LocalDateTime timeUpdated){
        metrics[property.routingNumber()]=property;
        this.timestamp = TimeUtil.toUTCMilliseconds(timeUpdated);
    }
    public MetricsSnapshot update(double currentData){
        ((MetricsProperty)metrics[trackingNumber-1]).value = currentData;
        this.timestamp = TimeUtil.toUTCMilliseconds(LocalDateTime.now());
        return this;
    }
    public Property push(Property property,LocalDateTime dateTime){
        Property toHistory = metrics[trackingNumber-1];
        for(int i=0;i<trackingNumber-1;i++){
            metrics[i]=metrics[i+1];
        }
        metrics[trackingNumber-1] = property;
        this.timestamp = TimeUtil.toUTCMilliseconds(dateTime);
        return toHistory;
    }
    public boolean validate(LocalDateTime current){
        LocalDateTime lastUpdated = TimeUtil.fromUTCMilliseconds(timestamp);
        //keep same day otherwise do reset
        return lastUpdated.getYear()==current.getYear()&&lastUpdated.getDayOfYear()==current.getDayOfYear()&&lastUpdated.getHour()==current.getHour();
    }
}
