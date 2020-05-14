package com.tarantula.platform.presence;

import com.tarantula.Account;

import java.util.Map;

public class UserAccount extends User implements Account {

    private String name;
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

        return properties;
    }
    public void fromMap(Map<String,Object> properties){
        this.routingNumber = ((Number) properties.get("5")).intValue();
    }
    @Override
    public String name() {
        return name;
    }

    @Override
    public void name(String name) {
        this.name = name;
    }
    public boolean trial(){
        return this.trial;
    }
    public void trial(boolean trial){
        this.trial = trial;
    }
}
