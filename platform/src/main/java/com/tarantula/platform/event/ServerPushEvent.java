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

    public ServerPushEvent(String source, String sessionId){
        this.source = source;
        this.sessionId = sessionId;
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
        out.writeUTF("1",this.bucket);
        out.writeUTF("2",this.source);
        out.writeUTF("3",this.sessionId);
        out.writeUTF("4",this.destination);
        out.writeUTF("6",this.clientId);//serverId
        out.writeUTF("7",this.owner);//node Id
        out.writeByteArray("9",this.payload);
    }
    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.bucket = in.readUTF("1");
        this.source = in.readUTF("2");
        this.sessionId = in.readUTF("3");
        this.destination = in.readUTF("4");
        this.clientId = in.readUTF("6");
        this.owner = in.readUTF("7");
        this.payload = in.readByteArray("9");
    }

    @Override
    public String toString(){
        return "Server Push Event ["+this.clientId+"]";
    }
}
