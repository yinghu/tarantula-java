package com.tarantula.platform.service.metrics;

import com.google.gson.JsonObject;
import com.icodesoftware.LeaderBoard;
import com.icodesoftware.Property;
import com.icodesoftware.Recoverable;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.AssociateKey;

import com.tarantula.platform.statistics.StatisticsPortableRegistry;

import java.time.LocalDateTime;
import java.util.Map;

public class MetricsHistory extends RecoverableObject  {

    final static int HOURLY_HISTORY_BUFFER_SIZE = 24;

    final static String LABEL_PREFIX = "history";


    private Property[] metrics;


    public MetricsHistory(int trackingNumber){
        this.routingNumber = trackingNumber;
        this.metrics = new Property[this.routingNumber];
    }

    public MetricsHistory(){

    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("trackingNumber",routingNumber);
        for(int i=0;i<routingNumber;i++){
            this.properties.put("m"+i,metrics[i].toJson().toString());
        }
        return this.properties;
    }

    @Override
    public void fromMap(Map<String,Object> properties){
        this.routingNumber = ((Number)properties.get("trackingNumber")).intValue();
        this.metrics = new Property[routingNumber];
        for(int i=0;i<routingNumber;i++){
            Object payload = properties.get("m"+i);
            JsonObject mj = JsonUtil.parse((String)payload);
            metrics[i]=(new MetricsProperty(i, mj.get("name").getAsString(), mj.get("value").getAsString(),mj.get("timestamp").getAsLong()));
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
    }
    @Override
    public int getFactoryId() {
        return StatisticsPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return StatisticsPortableRegistry.METRICS_HISTORY_CID;
    }

    public Key key(){
        return new AssociateKey(this.bucket,oid,label);
    }

    public void archiveHourly(Property property){
        int hour  = TimeUtil.fromUTCMilliseconds(property.timestamp()).getHour();
        metrics[hour>0?(hour-1):routingNumber-1]= property;
    }

    public void initializeHourly(LocalDateTime current){
        LocalDateTime start = current.minusHours(current.getHour());
        for(int i=0;i<routingNumber;i++){
            archiveHourly(new MetricsProperty(i,"h"+i,0,start.plusHours(i)));
        }
    }

}
