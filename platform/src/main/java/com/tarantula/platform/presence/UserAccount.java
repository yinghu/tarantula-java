package com.tarantula.platform.presence;

import com.tarantula.Account;

import java.util.Map;

public class UserAccount extends User implements Account {


    private int userCount;
    private int gameClusterCount;
    private boolean trial;
    private boolean subscribed;
    private long startTimestamp;
    private long endTimestamp;

    public UserAccount(){
        this.vertex = "Account";
    }

    public int getFactoryId() {
        return UserPortableRegistry.OID;
    }


    public int getClassId() {
        return UserPortableRegistry.USER_ACCOUNT_CID;
    }
    public Map<String,Object> toMap(){
        properties.put("1",emailAddress);
        properties.put("2",userCount);
        properties.put("3",gameClusterCount);
        properties.put("4",trial);
        properties.put("5",subscribed);
        properties.put("6",startTimestamp);
        properties.put("7",endTimestamp);
        properties.put("8",timestamp);
        return properties;
    }
    public void fromMap(Map<String,Object> properties){
        this.emailAddress = (String)properties.get("1");
        this.userCount = ((Number) properties.get("2")).intValue();
        this.gameClusterCount = ((Number) properties.get("3")).intValue();
        this.trial = (boolean )properties.get("4");
        this.subscribed = (boolean )properties.get("5");
        this.startTimestamp = ((Number) properties.get("6")).longValue();
        this.endTimestamp = ((Number) properties.get("7")).longValue();
        this.timestamp = ((Number) properties.get("8")).longValue();
    }
    public int userCount(int delta){
        this.userCount = this.userCount+delta;
        return this.userCount;
    }
    public int gameClusterCount(int delta){
        this.gameClusterCount = this.gameClusterCount+delta;
        return this.gameClusterCount;
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
