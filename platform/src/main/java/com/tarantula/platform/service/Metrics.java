package com.tarantula.platform.service;

import com.tarantula.platform.RecoverableObject;
import com.tarantula.platform.service.cluster.PortableRegistry;

import java.util.Map;

public class Metrics extends RecoverableObject {

    public final static String TOTAL_REQUESTS = "1";
    public final static String TOTAL_EVENTS = "2";


    public double totalRequests;
    public double totalEvents;

    public int getFactoryId() {
        return PortableRegistry.OID;
    }

    public int getClassId() {
        return PortableRegistry.METRICS_CID;
    }


    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",totalRequests);
        this.properties.put("2",totalEvents);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.totalRequests = ((Number)properties.get("1")).doubleValue();
        this.totalEvents = ((Number)properties.get("2")).doubleValue();
    }
}
