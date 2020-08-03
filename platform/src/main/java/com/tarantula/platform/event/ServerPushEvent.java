package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;

import java.io.IOException;

/**
 * Created by yinghu on 7/25//2020.
 */
public class ServerPushEvent extends Data implements EventOnAction {

    public ServerPushEvent(){

    }

    public ServerPushEvent(String source, String sessionId,String serverId,byte[] payload){
        this.source = source;
        this.sessionId = sessionId;
        this.trackId = serverId;
        this.payload = payload;
    }
    public ServerPushEvent(String source, String sessionId,String serverId,String clientId,byte[] payload){
        this.source = source;
        this.sessionId = sessionId;
        this.trackId = serverId;
        this.clientId = clientId;
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
        out.writeByteArray("5",this.payload);
    }
    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.source = in.readUTF("1");
        this.sessionId = in.readUTF("2");
        this.trackId = in.readUTF("3");
        this.clientId = in.readUTF("4");
        this.payload = in.readByteArray("5");
    }

    @Override
    public String toString(){
        return "Server Push Event ["+this.clientId+"]";
    }
}
