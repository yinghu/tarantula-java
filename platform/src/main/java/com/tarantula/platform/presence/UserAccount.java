package com.tarantula.platform.presence;

import com.tarantula.Account;

import java.util.Map;

public class UserAccount extends User implements Account {

    private String name;
    private int userCount;
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
        properties.put("3",trial);
        return properties;
    }
    public void fromMap(Map<String,Object> properties){
        this.name = (String)properties.get("1");
        this.userCount = ((Number) properties.get("2")).intValue();
        this.trial = (boolean )properties.get("3");
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
    public boolean trial(){
        return this.trial;
    }
    public void trial(boolean trial){
        this.trial = trial;
    }
}
