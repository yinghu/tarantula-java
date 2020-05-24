package com.tarantula.platform.presence;

import com.tarantula.Account;

import java.util.Map;

public class UserAccount extends User implements Account {


    private int userCount;
    private int gameClusterCount;
    private boolean trial;
    private boolean subscribed;

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
        properties.put("4",timestamp);
        properties.put("5",trial);
        properties.put("6",subscribed);
        return properties;
    }
    public void fromMap(Map<String,Object> properties){
        this.emailAddress = (String)properties.get("1");
        this.userCount = ((Number) properties.get("2")).intValue();
        this.gameClusterCount = ((Number) properties.get("3")).intValue();
        this.timestamp = ((Number) properties.get("4")).longValue();
        this.trial = (Boolean) properties.get("5");
        this.subscribed = (Boolean) properties.get("6");;
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

}
