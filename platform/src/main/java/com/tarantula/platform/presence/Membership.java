package com.tarantula.platform.presence;

import com.icodesoftware.Subscription;
import com.icodesoftware.util.RecoverableObject;

import java.util.Map;

public class Membership extends RecoverableObject implements Subscription {


    private long startTimestamp;
    private long endTimestamp;
    private int count;
    private boolean trial;
    public int getFactoryId() {
        return UserPortableRegistry.OID;
    }


    public synchronized int count(int delta){
        return this.count=this.count+(delta);
    }
    public int getClassId() {
        return UserPortableRegistry.MEMBERSHIP_CID;
    }
    public Map<String,Object> toMap(){
        properties.put("1",startTimestamp);
        properties.put("2",endTimestamp);
        properties.put("3",timestamp);
        properties.put("4",count);
        properties.put("5",trial);
        return properties;
    }
    public void fromMap(Map<String,Object> properties){
        this.startTimestamp = ((Number) properties.get("1")).longValue();
        this.endTimestamp = ((Number) properties.get("2")).longValue();
        this.timestamp = ((Number) properties.get("3")).longValue();
        this.count = ((Number) properties.getOrDefault("4",0)).intValue();
        this.trial = (boolean) properties.getOrDefault("5",false);
    }

    public boolean write(DataBuffer buffer){
        buffer.writeInt(count);
        buffer.writeLong(startTimestamp);
        buffer.writeLong(endTimestamp);
        buffer.writeLong(timestamp);
        buffer.writeBoolean(true);
        return true;
    }
    public boolean read(DataBuffer buffer) {
        this.count = buffer.readInt();
        this.startTimestamp = buffer.readLong();
        this.endTimestamp = buffer.readLong();
        this.timestamp = buffer.readLong();
        this.trial = buffer.readBoolean();
        return true;
    }
    public boolean trial(){
        return trial;
    }
    public void trial(boolean trial){
        this.trial = trial;
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
