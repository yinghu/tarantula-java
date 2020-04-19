package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.Event;

import java.io.IOException;

/**
 * Created by yinghu on 7/15//2019.
 */
public class ConnectionCloseEvent extends Data implements Event {



    public ConnectionCloseEvent(){

    }

    public ConnectionCloseEvent(String destination, String serverId){
        this.destination = destination;
        this.trackId = serverId;
    }
    @Override
    public int getFactoryId(){
        return PortableEventRegistry.OID;
    }
    @Override
    public int getClassId(){
        return PortableEventRegistry.CONNECTION_CLOSE_EVENT_CID;
    }
    @Override
    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1",this.trackId);
        out.writeUTF("2",this.destination);
        out.writeByteArray("3",this.payload);
    }
    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.trackId = in.readUTF("1");
        this.destination = in.readUTF("2");
        this.payload = in.readByteArray("3");
    }

    @Override
    public String toString(){
        return "Connection Close Event ["+this.trackId+"]";
    }
    @Override
    public int hashCode(){
        return this.trackId.hashCode();
    }
    @Override
    public boolean equals(Object obj){
        ConnectionCloseEvent ix = (ConnectionCloseEvent)obj;
        return this.trackId.equals(ix.trackId());
    }
}
