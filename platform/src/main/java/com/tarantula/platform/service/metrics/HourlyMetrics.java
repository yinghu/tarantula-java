package com.tarantula.platform.service.metrics;

import com.google.gson.JsonObject;
import com.icodesoftware.Property;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.ResourceKey;
import com.tarantula.platform.statistics.StatisticsPortableRegistry;

import java.util.Map;

public class HourlyMetrics extends RecoverableObject {

    private Property[] metrics;
    private int trackingNumber;

    public HourlyMetrics(int trackingNumber){
        this.trackingNumber = trackingNumber;
        this.metrics = new Property[trackingNumber];
    }

    public HourlyMetrics(){

    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("name",name);
        this.properties.put("trackingNumber",trackingNumber);
        for(int i=0;i<trackingNumber;i++){
            this.properties.put("m"+i,metrics[i].toJson().toString());
        }
        return this.properties;
    }

    @Override
    public void fromMap(Map<String,Object> properties){
        this.name = (String) properties.get("name");
        this.trackingNumber = ((Number)properties.get("trackingNumber")).intValue();
        this.metrics = new Property[trackingNumber];
        for(int i=0;i<trackingNumber;i++){
            JsonObject mj = JsonUtil.parse((String)properties.get("m"+i));
            metrics[i] = new MetricsProperty(i,mj.get("name").getAsString(),mj.get("value").getAsString());
        }
    }


    public Property[] metrics(){
        return metrics;
    }

    @Override
    public int getFactoryId() {
        return StatisticsPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return StatisticsPortableRegistry.HOURLY_METRICS_CID;
    }

    public Key key(){
        return new ResourceKey(this.bucket,oid,new String[]{"hourly",name});
    }

    public void property(Property property){
        metrics[property.routingNumber()]=property;
    }
}
