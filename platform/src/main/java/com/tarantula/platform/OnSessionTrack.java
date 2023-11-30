package com.tarantula.platform;

import com.google.gson.JsonObject;
import com.icodesoftware.OnSession;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.service.cluster.PortableRegistry;

import java.time.LocalDateTime;
import java.util.Map;


public class OnSessionTrack extends OnApplicationHeader implements OnSession {

    private String token;

    private String login;

    public static final OnSession PASSWORD_NOT_MATCHED = new OnSessionTrack("PASSWORD NOT MATCHED");
    public static final OnSession INVALID_TOKEN = new OnSessionTrack("INVALID TOKEN");

    public static final OnSession SESSION_NOT_AVAILABLE = new OnSessionTrack("SESSION NOT AVAILABLE");

    private int tournamentSlot;

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
   // public OnSessionTrack(long systemId,int stub,String ticket,String index){
      //  this();
        //this.distributionId = systemId;
        //this.stub = stub;
        //this.ticket = ticket;
        //this.index = index;
    //}

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
        jp.addProperty("SystemId",distributionId);
        jp.addProperty("Stub",stub);
        jp.addProperty("Token",token);
        jp.addProperty("Login",login);
        return jp;
    }

    public boolean write(DataBuffer buffer){
        buffer.writeInt(tournamentSlot);
        buffer.writeLong(tournamentId);
        buffer.writeLong(timestamp);
        buffer.writeBoolean(disabled);
        return true;
    }
    public boolean read(DataBuffer buffer) {
        this.tournamentSlot = buffer.readInt();
        this.tournamentId = buffer.readLong();
        this.timestamp = buffer.readLong();
        this.disabled = buffer.readBoolean();
        return true;
    }

    public int tournamentSlot(){
        return tournamentSlot;
    }
    public void onTournament(int tournamentSlot,long tournamentId){
        this.tournamentSlot = tournamentSlot;
        this.tournamentId = tournamentId;
        this.timestamp = TimeUtil.toUTCMilliseconds(LocalDateTime.now());
    }
}
