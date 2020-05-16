package com.tarantula.platform.presence;

import com.tarantula.Account;

import java.util.Map;

public class UserAccount extends User implements Account {

    private String name;
    private int userCount;
    private int gameClusterCount;
    private boolean trial;

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
        properties.put("1",name);
        properties.put("2",userCount);
        properties.put("3",gameClusterCount);
        properties.put("4",trial);
        return properties;
    }
    public void fromMap(Map<String,Object> properties){
        this.name = (String)properties.get("1");
        this.userCount = ((Number) properties.get("2")).intValue();
        this.gameClusterCount = ((Number) properties.get("3")).intValue();
        this.trial = (boolean )properties.get("4");
    }
    @Override
    public String name() {
        return name;
    }

    @Override
    public void name(String name) {
        this.name = name;
    }
    public int userCount(){
        return userCount;
    }
    public void userCount(int userCount){
        this.userCount = userCount;
    }
    public int gameClusterCount(){
        return gameClusterCount;
    }
    public void gameClusterCount(int gameClusterCount){
        this.gameClusterCount = gameClusterCount;
    }
    public boolean trial(){
        return this.trial;
    }
    public void trial(boolean trial){
        this.trial = trial;
    }
}
