package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Connection;
import com.icodesoftware.Event;
import com.icodesoftware.service.EventService;
import com.tarantula.platform.ClientConnection;
import com.tarantula.platform.UniverseConnection;
import com.tarantula.platform.service.ConnectionEventService;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by yinghu on 7/25//2020.
 */
public class ServerPushEvent extends Data implements Event {


    private AtomicBoolean lastAck;
    private Connection connection;
    public ServerPushEvent(){
        lastAck = new AtomicBoolean(true);
    }

    public ServerPushEvent(String source, String sessionId,String serverId,byte[] payload){
        this();
        this.source = source;
        this.sessionId = sessionId;
        this.trackId = serverId;
        this.payload = payload;
    }
    public ServerPushEvent(String source, String sessionId,String serverId,String clientId,String typeId,byte[] payload){
        this();
        this.source = source;
        this.sessionId = sessionId;
        this.trackId = serverId;
        this.clientId = clientId;
        this.typeId = typeId;
        this.payload = payload;
    }
    @Override
    public int getFactoryId(){
        return PortableEventRegistry.OID;
    }
    @Override
    public int getClassId(){
        return PortableEventRegistry.SERVER_PUSH_EVENT_CID;
    }
    @Override
    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1",this.source);
        out.writeUTF("2",this.sessionId);
        out.writeUTF("3",this.trackId);//serverId
        out.writeUTF("4",this.clientId);
        out.writeUTF("5",this.typeId);
        out.writeByteArray("6",this.payload);
    }
    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.source = in.readUTF("1");
        this.sessionId = in.readUTF("2");
        this.trackId = in.readUTF("3");
        this.clientId = in.readUTF("4");
        this.typeId = in.readUTF("5");
        this.payload = in.readByteArray("6");
    }
    public EventService eventService(){
        return this.eventService;
    }

    public void onMessage(byte[] payload,String label,Connection connection){
        ((ConnectionEventService)eventService).publish(payload,label,connection);
    }
    public void clear(){

    }
    public void ack(){
        lastAck.set(true);
    }
    public boolean check(){
        return lastAck.getAndSet(false);
    }
    @Override
    public String toString(){
        return "Server Push Event ["+this.typeId+"]";
    }
    @Override
    public void write(byte[] delta,int batch,String contentType){
        //this.write(delta,batch,contentType,label,false);
    }
    @Override
    public void write(byte[] payload,int batch,String contentType,boolean closed){
        //this.eventService.publish(new ResponsiveEvent(this.source,this.sessionId,payload,batch,contentType,label,closed));
    }
    @Override
    public void write(byte[] message){ }
    @Override
    public void write(byte[] payload,boolean closed){ }
    public void connection(Connection connection){
        this.connection = connection;
    }
    public Connection connection(){
        ClientConnection conn = new ClientConnection();
        conn.host(this.connection.host());
        conn.port(this.connection.port());
        conn.type(this.connection.type());
        conn.secured(this.connection.secured());
        conn.serverId(this.connection.serverId());
        return conn;
    }
}
