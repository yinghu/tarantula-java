package com.tarantula.platform;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.OnSession;
import com.tarantula.platform.service.cluster.PortableRegistry;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Updated by yinghu lu on 8/20/19
 */
public class OnSessionTrack extends OnApplicationHeader implements OnSession {

    private String token;

    private String login;
    private String ticket;

    private int totalSessions;
    private int activeSessions;

    public static final OnSession ON_SESSION_NOT_AVAILABLE = new OnSessionTrack("ON SESSION NOT AVAILABLE");
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
    public OnSessionTrack(String systemId,int stub,String ticket,String oid,int routingNumber){
        this();
        this.systemId = systemId;
        this.stub = stub;
        this.ticket = ticket;
        this.oid = oid;
        this.routingNumber =routingNumber;
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
    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1",this.oid);
        out.writeUTF("2",systemId);
        out.writeInt("3",this.stub);
        out.writeLong("4",this.timestamp);
        out.writeBoolean("6",this.disabled);
    }

    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.oid = in.readUTF("1");
        this.systemId = in.readUTF("2");
        this.stub = in.readInt("3");
        this.timestamp = in.readLong("4");
        this.disabled = in.readBoolean("6");
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
        buffer.putInt(totalSessions);
        buffer.putInt(activeSessions);
        buffer.putInt(stub);
        buffer.putLong(timestamp);
        return buffer.array();
    }
    @Override
    public void fromByteArray(byte[] data){
        ByteBuffer buffer = ByteBuffer.wrap(data);
        this.totalSessions = buffer.getInt();
        this.activeSessions = buffer.getInt();
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

    public boolean online(){
        return activeSessions>0;
    }

    public int activeSessions(int delta){
        this.activeSessions = activeSessions+(delta);
        if(this.activeSessions<0){
            activeSessions = 0;
        }
        if(delta>0){
            totalSessions=totalSessions+delta;
        }
        return this.activeSessions;
    }
    public int totalSessions(){
        return this.totalSessions;
    }

    @Override
    public Key key(){
        return new AssociateKey(this.bucket,this.oid,this.vertex);
    }
    public String toString(){
        return "TotalSession["+totalSessions+"]ActiveSessions["+activeSessions+"]Stub["+stub+"]";
    }
}
