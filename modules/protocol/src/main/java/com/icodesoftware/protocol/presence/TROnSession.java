package com.icodesoftware.protocol.presence;

import com.google.gson.JsonObject;
import com.icodesoftware.OnSession;
import com.icodesoftware.util.TROnApplication;


public class TROnSession extends TROnApplication implements OnSession {

    protected String token;

    protected String login;
    protected String role = TRRole.player.name();

    public static final OnSession PASSWORD_NOT_MATCHED = new TROnSession("PASSWORD NOT MATCHED");
    public static final OnSession INVALID_TOKEN = new TROnSession("INVALID TOKEN");
    public static final OnSession INVALID_TICKET = new TROnSession("INVALID TICKET");
    public static final OnSession SESSION_NOT_AVAILABLE = new TROnSession("SESSION NOT AVAILABLE");


    public TROnSession(){
        this.onEdge = true;
        this.label = LABEL;
    }
    public TROnSession(String message){
        this();
        this.message = message;
        this.successful = false;
    }
    public TROnSession(long systemId, long stub){
        this();
        this.distributionId = systemId;
        this.stub = stub;
        this.successful = true;
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
    public String role(){
        return role;
    }

    public void role(String role){
        this.role = role;
    }
    public String toString(){
        return "OnSession->["+token+"]";
    }

    @Override
    public JsonObject toJson() {
        JsonObject jp = new JsonObject();
        jp.addProperty("Successful",true);
        jp.addProperty("SystemId",Long.toString(distributionId));
        jp.addProperty("Stub",Long.toString(stub));
        jp.addProperty("Token",token);
        jp.addProperty("Login",login);
        return jp;
    }

}
