package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.Event;

import java.io.IOException;

/**
 * Created by yinghu on 6/18/2020.
 */
public class AccessIndexStateEvent extends Data implements Event {



    public AccessIndexStateEvent(){

    }

    public AccessIndexStateEvent(String destination, String serverId, boolean closed){
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
        return PortableEventRegistry.ACCESS_INDEX_STATE_EVENT_CID;
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
        return "Access Index State Event ["+this.trackId+"]["+closed+"]";
    }
    @Override
    public int hashCode(){
        return this.trackId.hashCode();
    }
    @Override
    public boolean equals(Object obj){
        AccessIndexStateEvent ix = (AccessIndexStateEvent)obj;
        return this.trackId.equals(ix.trackId());
    }
}
