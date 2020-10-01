package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Connection;
import com.icodesoftware.Event;
import com.icodesoftware.protocol.PendingInboundMessage;
import com.icodesoftware.service.EventService;
import com.tarantula.platform.service.ConnectionEventService;

import javax.crypto.Cipher;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yinghu on 7/25//2020.
 */
public class ServerPushEvent extends Data implements Event {

    private ConcurrentHashMap<Long, Connection> cMap = new ConcurrentHashMap<>();
    private Cipher decrypt;
    public ServerPushEvent(){

    }

    public ServerPushEvent(String source, String sessionId,String serverId,byte[] payload){
        this.source = source;
        this.sessionId = sessionId;
        this.trackId = serverId;
        this.payload = payload;
    }
    public ServerPushEvent(String source, String sessionId,String serverId,String clientId,String typeId,byte[] payload){
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
    public void addConnection(Connection connection){
        cMap.put(connection.connectionId(),connection);
    }
    public void cipher(Cipher decrypt){
        this.decrypt = decrypt;
    }
    public void removeConnection(long connectionId){
        cMap.remove(connectionId);
    }
    public void onMessage(PendingInboundMessage pendingInboundMessage){
        //process message
        try{
            ByteBuffer buffer = ByteBuffer.wrap(decrypt.doFinal(pendingInboundMessage.sequence()));
            System.out.println("SEQUENCE->"+buffer.getInt());
            Connection connection = cMap.get(pendingInboundMessage.connectionId());
            connection.update(pendingInboundMessage.type(),pendingInboundMessage.payload());
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    public void onMessage(byte[] payload,String label,Connection connection){
        ((ConnectionEventService)eventService).publish(payload,label,connection);
    }
    public void clear(){

    }
    @Override
    public String toString(){
        return "Server Push Event ["+this.typeId+"]";
    }
    @Override
    public void write(byte[] delta,int batch,String contentType,String label){
        //this.write(delta,batch,contentType,label,false);
    }
    @Override
    public void write(byte[] payload,int batch,String contentType,String label,boolean closed){
        //this.eventService.publish(new ResponsiveEvent(this.source,this.sessionId,payload,batch,contentType,label,closed));
    }
    @Override
    public void write(byte[] message,String label){ }
    @Override
    public void write(byte[] payload,String label,boolean closed){ }
}
