package com.tarantula.platform.presence;

import com.tarantula.Subscription;
import com.tarantula.platform.RecoverableObject;

import java.util.Map;

public class Membership extends RecoverableObject implements Subscription {


    private boolean trial;
    private boolean subscribed;
    private long startTimestamp;
    private long endTimestamp;

    public int getFactoryId() {
        return UserPortableRegistry.OID;
    }


    public int getClassId() {
        return UserPortableRegistry.USER_ACCOUNT_CID;
    }
    public Map<String,Object> toMap(){
        properties.put("1",trial);
        properties.put("2",subscribed);
        properties.put("3",startTimestamp);
        properties.put("4",endTimestamp);
        properties.put("5",timestamp);
        return properties;
    }
    public void fromMap(Map<String,Object> properties){
        this.trial = (Boolean) properties.get("1");
        this.subscribed = (Boolean) properties.get("2");;
        this.startTimestamp = ((Number) properties.get("3")).longValue();
        this.endTimestamp = ((Number) properties.get("4")).longValue();
        this.timestamp = ((Number) properties.get("5")).longValue();
    }
    public boolean trial(){
        return this.trial;
    }
    public void trial(boolean trial){
        this.trial = trial;
    }

    public boolean subscribed(){
        return this.subscribed;
    }
    public void subscribed(boolean subscribed){
        this.subscribed = subscribed;
    }

    public long startTimestamp(){
        return this.startTimestamp;
    }
    public long endTimestamp(){
        return this.endTimestamp;
    }
    public void startTimestamp(long startTimestamp){
        this.startTimestamp = startTimestamp;
    }
    public void endTimestamp(long endTimestamp){
        this.endTimestamp = endTimestamp;
    }
}
