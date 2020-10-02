package com.tarantula.platform;


import com.icodesoftware.OnInstance;
import com.tarantula.platform.service.cluster.PortableRegistry;

import java.util.Map;

/**
 * Developer: YINGHU LU
 * Date Updated: 8/11/2019
 */
public class OnInstanceTrack extends OnApplicationHeader implements OnInstance {


    private boolean joined;
    private int idle;

    public OnInstanceTrack(){
        this.label = LABEL;
        this.onEdge = true;
    }

    public OnInstanceTrack(String systemId,int stub,String applicationId,String instanceId,boolean joined){
        this();
        this.systemId = systemId;
        this.stub = stub;
        this.applicationId = applicationId;
        this.instanceId = instanceId;
        this.joined = joined;
    }

    @Override
    public int getFactoryId() {
        return PortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PortableRegistry.ON_INSTANCE_CID;
    }

    @Override
    public String toString(){
        return "["+this.systemId+","+this.stub+"]["+"On Instance "+this.applicationId+","+this.instanceId+","+this.joined+"]";
    }


    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1", systemId);
        this.properties.put("2",stub);
        this.properties.put("3",joined);
        this.properties.put("6",balance);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.systemId = (String)properties.get("1");
        this.stub = ((Number)properties.get("2")).intValue();
        this.joined = (Boolean)properties.get("3");
        this.balance = ((Number)properties.get("6")).doubleValue();
    }
    public  boolean joined(){
        return this.joined;
    }
    public void joined(boolean joined) {
        this.joined = joined;
    }


    public void reset(double reset){
        this.balance = reset;
    }


    public boolean transact(double delta){
        if((balance+(delta)>=0)){
            balance  = balance+(delta);
            this.dataStore.update(this);
            return true;
        }
        else{
            return false;
        }
    }
    public synchronized int idle(boolean reset){
        this.idle = reset?0:(this.idle+1);
        return this.idle;
    }

}
