package com.tarantula.platform.presence;

import com.icodesoftware.util.RecoverableObject;

import java.util.Map;

public class DeveloperLogin extends RecoverableObject {

    public static final String DataStore = "developer_login";

    public DeveloperLogin(){

    }
    public DeveloperLogin(String provider, String password, String deviceId){
        this.owner = provider;
        this.index = password;
        this.name = deviceId;
    }
    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PresencePortableRegistry.DEVELOPER_LOGIN_CID;
    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",owner);
        this.properties.put("2",index);
        this.properties.put("3",name);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.owner = (String)properties.get("1");
        this.index = (String)properties.get("2");
        this.name = (String)properties.get("3");
    }

    public String provider(){
        return owner;
    }
    public String password(){
        return index;
    }
    public String deviceId(){
        return name;
    }
}
