package com.icodesoftware.service;

import com.icodesoftware.OnSession;

import java.util.Map;

/**
 * Updated by yinghu lu on 8/20/19
 */
public class OnSessionTrack extends OnApplicationHeader implements OnSession {

    private String token;

    private String login;
    private String ticket;

    public static final OnSession PASSWORD_NOT_MATCHED = new OnSessionTrack("PASSWORD NOT MATCHED");
    //public static final OnSession TOKEN_NOT_VALID = new OnSessionTrack("TOKEN NOT VALID");

    public OnSessionTrack(){
        //this.vertex = "OnSession";
    }
    public OnSessionTrack(String msg){
        //this();
        this.message = msg;
        this.successful = false;
    }
    public OnSessionTrack(String systemId, double balance){
        this();
        this.systemId = systemId;
        this.balance = balance;
    }
    public OnSessionTrack(String systemId, int stub, String ticket){
        this();
        this.systemId = systemId;
        this.stub = stub;
        this.ticket = ticket;
    }
    public OnSessionTrack(String systemId, int stub){
        this();
        this.systemId = systemId;
        this.stub = stub;
    }
    @Override
    public int getFactoryId() {
        return 1;
        //return PortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return 11;
        //return PortableRegistry.ON_SESSION_CID;
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

    public String ticket(){
        return this.ticket;
    }
    public void ticket(String ticket){
        this.ticket = ticket;
    }

    public String toString(){
        return "OnSession->["+token+"]";
    }
}
