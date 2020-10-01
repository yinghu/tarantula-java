package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Event;

import java.io.IOException;

/**
 * Updated by yinghu on 4/30/2020.
 */
public class ConnectionStateEvent extends Data implements Event {



    public ConnectionStateEvent(){

    }

    public ConnectionStateEvent(String destination, String serverId,boolean closed){
        this.destination = destination;
        this.trackId = serverId;
        this.closed = closed;
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
        out.writeBoolean("1",this.closed);
        out.writeUTF("2",this.trackId);
        out.writeUTF("3",this.destination);
        out.writeByteArray("4",this.payload);
    }
    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.closed = in.readBoolean("1");
        this.trackId = in.readUTF("2");
        this.destination = in.readUTF("3");
        this.payload = in.readByteArray("4");
    }

    @Override
    public String toString(){
        return "Connection State Event ["+this.trackId+"]["+closed+"]";
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
