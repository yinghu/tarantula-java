package com.tarantula.platform.presence;

import com.tarantula.Subscription;
import com.tarantula.platform.RecoverableObject;

import java.util.Map;

public class Membership extends RecoverableObject implements Subscription {


    private long startTimestamp;
    private long endTimestamp;

    public int getFactoryId() {
        return UserPortableRegistry.OID;
    }


    public int getClassId() {
        return UserPortableRegistry.USER_ACCOUNT_CID;
    }
    public Map<String,Object> toMap(){
        properties.put("1",startTimestamp);
        properties.put("2",endTimestamp);
        properties.put("3",timestamp);
        return properties;
    }
    public void fromMap(Map<String,Object> properties){
        this.startTimestamp = ((Number) properties.get("1")).longValue();
        this.endTimestamp = ((Number) properties.get("2")).longValue();
        this.timestamp = ((Number) properties.get("3")).longValue();
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
