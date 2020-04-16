package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.EventOnAction;

import java.io.IOException;

/**
 * Created by yinghu on 7/15//2019.
 */
public class ConnectionStateEvent extends Data implements EventOnAction {



    public ConnectionStateEvent(){

    }

    public ConnectionStateEvent(String destination, String serverId, boolean disabled){
        this.destination = destination;
        this.trackId = serverId;
        this.disabled = disabled;
    }
    @Override
    public int getFactoryId(){
        return PortableEventRegistry.OID;
    }
    @Override
    public int getClassId(){
        return PortableEventRegistry.CONNECTION_STATE_EVENT_CID;
    }
    @Override
    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1",this.destination);
        out.writeUTF("2",this.trackId);
        out.writeUTF("3",this.owner);//node Id
        out.writeBoolean("4",this.disabled);

    }
    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.destination = in.readUTF("1");
        this.trackId = in.readUTF("2");
        this.owner = in.readUTF("3");
        this.disabled = in.readBoolean("4");
    }

    @Override
    public String toString(){
        return "Connection State Event ["+this.trackId+"/"+disabled+"/"+owner;
    }
    @Override
    public int hashCode(){
        return this.trackId.hashCode();
    }
    @Override
    public boolean equals(Object obj){
        ConnectionStateEvent ix = (ConnectionStateEvent)obj;
        return this.trackId.equals(ix.trackId());
    }
}
