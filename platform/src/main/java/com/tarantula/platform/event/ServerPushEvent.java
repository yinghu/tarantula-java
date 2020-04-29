package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;

import java.io.IOException;

/**
 * Created by yinghu on 7/15//2019.
 */
public class ServerPushEvent extends Data implements EventOnAction {



    public ServerPushEvent(){

    }

    public ServerPushEvent(String source, String sessionId,boolean disabled){
        this.source = source;
        this.sessionId = sessionId;
        this.disabled = disabled;
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
        out.writeUTF("5",this.trackId);
        out.writeUTF("6",this.clientId);//serverId
        out.writeUTF("7",this.owner);//node Id
        out.writeBoolean("8",this.disabled);
        out.writeByteArray("9",this.payload);
    }
    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.bucket = in.readUTF("1");
        this.source = in.readUTF("2");
        this.sessionId = in.readUTF("3");
        this.destination = in.readUTF("4");
        this.trackId = in.readUTF("5");
        this.clientId = in.readUTF("6");
        this.owner = in.readUTF("7");
        this.disabled = in.readBoolean("8");
        this.payload = in.readByteArray("9");
    }

    @Override
    public String toString(){
        return "Server Push Event ["+this.sessionId+"/"+disabled+"/"+owner;
    }
    @Override
    public int hashCode(){
        return this.sessionId.hashCode();
    }
    @Override
    public boolean equals(Object obj){
        ServerPushEvent ix = (ServerPushEvent)obj;
        return this.sessionId.equals(ix.sessionId());
    }
}
