package com.tarantula.demo;

import com.tarantula.Recoverable;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.RecoverableObject;

import java.util.Map;

/**
 * Updated 7/25/19 by yinghu
 */
public class Timer extends RecoverableObject {

    public long hour;
    public long minute;
    public long second;
    public long millisecond;

    public long delta = 50;
    public long duration;


    public Timer(){
        this.vertex = "Timer";
        this.label = "timer";
    }
    public Timer(long duration,long delta){
        this();
        this.duration = duration;
        this.delta = delta;
        this.timestamp = duration;
    }

    @Override
    public Map<String,Object> toMap(){
        properties.put("duration",duration);
        properties.put("timestamp",this.timestamp);
        properties.put("delta",this.delta);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.duration = ((Number)properties.get("duration")).longValue();
        this.timestamp = ((Number)properties.get("timestamp")).longValue();
        if(properties.get(delta)!=null){
            delta = ((Number)properties.get("delta")).longValue();
        }
    }

    @Override
    public int getFactoryId() {
        return DemoPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return DemoPortableRegistry.TIMER_OID;
    }
    public Recoverable.Key key(){
        return new AssociateKey(this.bucket,this.oid,this.vertex);
    }

    public synchronized void update(){
        timestamp -= delta;
        if(timestamp<=0){
            timestamp = duration;
        }
        hour = (timestamp/3600000);
        long hr = timestamp%3600000;
        minute = hr/60000;
        long mr = hr%60000;
        second = mr/1000;
        millisecond = mr%1000;
    }
    public String toString(){
        return "["+duration+"/"+delta+"/"+timestamp+"]";
    }
}
