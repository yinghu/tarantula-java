package com.tarantula.platform.service.metrics;

import com.google.gson.JsonObject;
import com.icodesoftware.Property;
import com.icodesoftware.Recoverable;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.ResourceKey;
import com.tarantula.platform.statistics.StatisticsPortableRegistry;

import java.util.Map;

public class MetricsHistory extends RecoverableObject  {

    final static String LABEL_PREFIX = "history";
    private Property[] metrics;
    private int trackingNumber;

    public MetricsHistory(int trackingNumber, String category, String classifier){
        this.trackingNumber = trackingNumber;
        this.name = category;
        this.index = classifier;
        this.metrics = new Property[trackingNumber];
    }

    public MetricsHistory(){

    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("trackingNumber",trackingNumber);
        for(int i=0;i<trackingNumber;i++){
            this.properties.put("m"+i,metrics[i].toJson().toString());
        }
        return this.properties;
    }

    @Override
    public void fromMap(Map<String,Object> properties){
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
    public void distributionKey(String rkey){
        String[] idx = rkey.split(Recoverable.PATH_SEPARATOR);
        bucket = idx[0];
        oid = idx[1];
        label = idx[2];
        //index = idx[2];
        //name = idx[3];
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
        return new AssociateKey(this.bucket,oid,label);
    }

    public void initialize(Property property){
        metrics[property.routingNumber()]=property;
    }
    public MetricsHistory update(double currentData){
        ((MetricsProperty)metrics[trackingNumber-1]).value = currentData;
        return this;
    }
    public Property push(Property property){
        Property toHistory = metrics[0];
        for(int i=0;i<trackingNumber-1;i++){
            metrics[i]=metrics[i+1];
        }
        metrics[trackingNumber-1] = property;
        return toHistory;
    }
}
