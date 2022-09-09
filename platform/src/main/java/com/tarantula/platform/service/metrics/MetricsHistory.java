package com.tarantula.platform.service.metrics;

import com.google.gson.JsonObject;
import com.icodesoftware.Property;
import com.icodesoftware.Recoverable;
import com.icodesoftware.util.FIFOBuffer;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.AssociateKey;

import com.tarantula.platform.statistics.StatisticsPortableRegistry;

import java.util.ArrayList;
import java.util.Map;

public class MetricsHistory extends RecoverableObject  {

    final static int HOURLY_HISTORY_BUFFER_SIZE = 24;

    final static String LABEL_PREFIX = "history";
    private int trackingNumber;

    private FIFOBuffer<Property> metrics;

    public MetricsHistory(int trackingNumber){
        this.trackingNumber = trackingNumber;
        this.metrics = new FIFOBuffer<>(trackingNumber,new Property[trackingNumber]);
    }

    public MetricsHistory(){

    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("trackingNumber",trackingNumber);
        int ix = 0;
        for(Property p : metrics.list(new ArrayList<>())){
            this.properties.put("m"+ix,p.toJson().toString());
            ix++;
        }
        return this.properties;
    }

    @Override
    public void fromMap(Map<String,Object> properties){
        this.trackingNumber = ((Number)properties.get("trackingNumber")).intValue();
        this.metrics = new FIFOBuffer<>(trackingNumber,new Property[trackingNumber]);
        for(int i=0;i<trackingNumber;i++){
            Object payload = properties.get("m"+i);
            if(payload!=null) {
                JsonObject mj = JsonUtil.parse((String)payload);
                metrics.push(new MetricsProperty(i, mj.get("name").getAsString(), mj.get("value").getAsString()));
            }
        }
    }


    public Property[] metrics(){
        return metrics.list(new Property[trackingNumber]);
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
        return StatisticsPortableRegistry.METRICS_HISTORY_CID;
    }

    public Key key(){
        return new AssociateKey(this.bucket,oid,label);
    }

    public void push(Property property){
        metrics.push(property);
    }
}
