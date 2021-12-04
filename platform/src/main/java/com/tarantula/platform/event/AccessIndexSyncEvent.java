package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Event;

import java.io.IOException;

public class AccessIndexSyncEvent extends Data implements Event {


    public AccessIndexSyncEvent(){

    }
    public AccessIndexSyncEvent(String destination,String key, byte[] value){
        this.destination = destination;
        this.trackId = key;
        this.payload = value;
    }
    @Override
    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1",this.destination);
        out.writeUTF("2",this.trackId);
        out.writeByteArray("3",this.payload);
    }
    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.destination = in.readUTF("1");
        this.trackId = in.readUTF("2");
        this.payload = in.readByteArray("3");
    }
    @Override
    public int getClassId() {
        return PortableEventRegistry.ACCESS_INDEX_SYNC_EVENT_CID;
    }
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }
    @Override
    public String toString(){
        return "Access index sync event ->["+trackId+">>>"+new String(payload)+"]";
    }
}
