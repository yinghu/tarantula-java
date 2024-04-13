package com.tarantula.platform;

import com.google.gson.JsonObject;
import com.icodesoftware.OnSession;
import com.icodesoftware.util.OnApplicationHeader;
import com.tarantula.platform.service.cluster.PortableRegistry;

public class OnSessionTrack extends OnApplicationHeader implements OnSession {

    private String token;

    private String login;

    public static final OnSession PASSWORD_NOT_MATCHED = new OnSessionTrack("PASSWORD NOT MATCHED");
    public static final OnSession INVALID_TOKEN = new OnSessionTrack("INVALID TOKEN");

    public static final OnSession SESSION_NOT_AVAILABLE = new OnSessionTrack("SESSION NOT AVAILABLE");



    public OnSessionTrack(){
        this.onEdge = true;
        this.label = LABEL;
    }
    public OnSessionTrack(String message){
        this();
        this.message = message;
        this.successful = false;
    }
    public OnSessionTrack(long systemId,long stub){
        this();
        this.distributionId = systemId;
        this.stub = stub;
        this.successful = true;
    }

    @Override
    public int getFactoryId() {
        return PortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableRegistry.ON_SESSION_CID;
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
        jp.addProperty("SystemId",distributionId);
        jp.addProperty("Stub",stub);
        jp.addProperty("Token",token);
        jp.addProperty("Login",login);
        return jp;
    }

}
