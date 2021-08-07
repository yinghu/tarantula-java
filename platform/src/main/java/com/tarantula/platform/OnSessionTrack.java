package com.tarantula.platform;

import com.icodesoftware.OnSession;
import com.tarantula.platform.service.cluster.PortableRegistry;

import java.util.Map;


public class OnSessionTrack extends OnApplicationHeader implements OnSession {

    private String token;

    private String login;

    public static final OnSession PASSWORD_NOT_MATCHED = new OnSessionTrack("PASSWORD NOT MATCHED");

    public OnSessionTrack(){
        //this.vertex = "OnSession";
    }
    public OnSessionTrack(String msg){
        //this();
        this.message = msg;
        this.successful = false;
    }
    public OnSessionTrack(String systemId,double balance){
        this();
        this.systemId = systemId;
        this.balance = balance;
    }
    public OnSessionTrack(String systemId,int stub,String ticket){
        this();
        this.systemId = systemId;
        this.stub = stub;
        this.ticket = ticket;
    }
    public OnSessionTrack(String systemId,int stub){
        this();
        this.systemId = systemId;
        this.stub = stub;
    }
    @Override
    public int getFactoryId() {
        return PortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableRegistry.ON_SESSION_CID;
    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",token);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.token = (String)properties.get("1");
    }

    public String token(){
        return this.token;
    }
    public void token(String token){
        this.token = token;
    }
    public String login(){
        return this.login;
    }
    public void login(String login){
        this.login = login;
    }


    public String toString(){
        return "OnSession->["+token+"]";
    }
}
