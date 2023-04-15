package com.tarantula.platform;

import com.google.gson.JsonObject;
import com.icodesoftware.OnSession;
import com.tarantula.platform.service.cluster.PortableRegistry;

import java.util.Map;


public class OnSessionTrack extends OnApplicationHeader implements OnSession {

    private String token;

    private String login;

    public static final OnSession PASSWORD_NOT_MATCHED = new OnSessionTrack("PASSWORD NOT MATCHED",false);

    public OnSessionTrack(){

    }

    public OnSessionTrack(String systemId,boolean successful){
        this();
        this.systemId = systemId;
        this.successful = successful;
    }
    public OnSessionTrack(String systemId,int stub,String ticket,String index){
        this();
        this.systemId = systemId;
        this.stub = stub;
        this.ticket = ticket;
        this.index = index;
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

    @Override
    public JsonObject toJson() {
        JsonObject jp = new JsonObject();
        jp.addProperty("Successful",true);
        jp.addProperty("SystemId",systemId);
        jp.addProperty("Stub",stub);
        jp.addProperty("Token",token);
        jp.addProperty("Ticket",ticket);
        jp.addProperty("Login",login);
        return jp;
    }
}
