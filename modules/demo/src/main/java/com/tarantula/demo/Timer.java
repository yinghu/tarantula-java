package com.tarantula.demo;

import com.tarantula.Recoverable;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.OnApplicationHeader;

import java.util.Map;

/**
 * Updated 7/25/19 by yinghu
 */
public class Timer extends OnApplicationHeader {

    public long hour;
    public long minute;
    public long second;
    public long millisecond;

    public long delta = 50;
    public long duration;

    public Timer(){
        this.vertex = "Timer";
        this.label = "timer";
        this.command = "timer";
    }
    public Timer(long duration,long delta){
        this();
        this.duration = duration;
        this.delta = delta;
        this.timestamp = duration;
    }
    public Timer(long h,long m,long s,long ms,long tm,long ss){
        this();
        this.hour = h;
        this.minute= m;
        this.second = s;
        this.millisecond = ms;
        this.timestamp = tm;
        this.sequence = ss;
    }

    @Override
    public Map<String,Object> toMap(){
        properties.put("duration",duration);
        properties.put("timestamp",this.timestamp);
        properties.put("delta",this.delta);
        properties.put("sequence",this.sequence);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.duration = ((Number)properties.get("duration")).longValue();
        this.timestamp = ((Number)properties.get("timestamp")).longValue();
        if(properties.get("sequence")!=null){
            this.sequence = ((Number)properties.get("sequence")).longValue();
        }
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

    public synchronized Timer update(){
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
        sequence++;
        return new Timer(hour,minute,second,millisecond,timestamp,sequence);
    }
    public String toString(){
        return "["+duration+"/"+delta+"/"+timestamp+"]";
    }
}
