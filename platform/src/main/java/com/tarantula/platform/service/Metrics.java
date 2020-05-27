package com.tarantula.platform.service;

import com.tarantula.Statistics;
import com.tarantula.platform.CompositeKey;
import com.tarantula.platform.OnApplicationHeader;
import com.tarantula.platform.service.cluster.PortableRegistry;


public class Metrics extends OnApplicationHeader {

    //metrics entry
    public final static String STATS_KEY = "a";
    public final static String START_TIME ="b";
    

    //stats entry
    public final static String REQUEST_COUNT = "1";
    public final static String EVENT_OUT_COUNT = "2";
    public final static String EVENT_IN_COUNT = "3";
    public final static String GOOGLE_COUNT = "4";
    public final static String STRIPE_COUNT = "5";

    public Statistics statistics;

    public Metrics(){
        this.vertex = "Metrics";
    }
    public Metrics(String nodeId){
        this();
        this.owner = nodeId;
    }
    public int getFactoryId() {
        return PortableRegistry.OID;
    }

    public int getClassId() {
        return PortableRegistry.METRICS_CID;
    }
    public void distributionKey(String distributionKey){
        //skip the natural key
    }
    public Key key(){
        return new CompositeKey(this.vertex,this.owner);
    }
}
