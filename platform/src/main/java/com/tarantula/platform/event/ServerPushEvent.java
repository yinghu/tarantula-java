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
        out.writeUTF("2",this.source);
        out.writeUTF("3",this.sessionId);
        out.writeUTF("6",this.trackId);//serverId
        out.writeByteArray("9",this.payload);
    }
    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.source = in.readUTF("2");
        this.sessionId = in.readUTF("3");
        this.trackId = in.readUTF("6");
        this.payload = in.readByteArray("9");
    }

    @Override
    public String toString(){
        return "Server Push Event ["+this.clientId+"]";
    }
}
