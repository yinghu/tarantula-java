package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.Event;
import com.tarantula.EventService;

import java.io.IOException;

/**
 * Created by yinghu on 7/25//2020.
 */
public class ServerPushEvent extends Data implements Event {

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
    @Override
    public String toString(){
        return "Server Push Event ["+this.typeId+"]";
    }
}
