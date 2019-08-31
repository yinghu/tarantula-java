package com.tarantula.platform;


import com.tarantula.OnSession;
import com.tarantula.platform.service.cluster.PortableRegistry;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Updated by yinghu lu on 8/20/19
 */
public class OnSessionTrack extends OnApplicationHeader implements OnSession {

    private String token;

    private String login;
    private String ticket;

    public static final OnSession PASSWORD_NOT_MATCHED = new OnSessionTrack("PASSWORD NOT MATCHED");

    public OnSessionTrack(){
        this.binary = true;
        this.vertex = "OnSession";
    }
    public OnSessionTrack(String msg){
        this();
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
        this.properties.put("systemId",systemId);
        this.properties.put("timestamp",this.timestamp);
        this.properties.put("disabled",this.disabled);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.systemId = (String)properties.get("systemId");
        this.timestamp = ((Number)properties.get("timestamp")).longValue();
        this.disabled = (boolean)properties.get("disabled");
    }
    @Override
    public byte[] toByteArray(){
        ByteBuffer buffer = ByteBuffer.allocate(20);
        //buffer.putInt(totalSessions);
        //buffer.putInt(activeSessions);
        buffer.putInt(stub);
        buffer.putLong(timestamp);
        return buffer.array();
    }
    @Override
    public void fromByteArray(byte[] data){
        ByteBuffer buffer = ByteBuffer.wrap(data);
        //this.totalSessions = buffer.getInt();
        //this.activeSessions = buffer.getInt();
        this.stub = buffer.getInt();
        this.timestamp = buffer.getLong();
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

    @Override
    public Key key(){
        return new AssociateKey(this.bucket,this.oid,this.vertex);
    }
    public String toString(){
        return "OnSession->"+systemId+"/"+stub;
    }
}
