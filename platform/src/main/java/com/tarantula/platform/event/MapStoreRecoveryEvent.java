package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.Event;

import java.io.IOException;

/**
 * Updated by yinghu lu on 6/28/2020
 */
public class MapStoreRecoveryEvent extends Data implements Event {


    public MapStoreRecoveryEvent(){
    }
    public MapStoreRecoveryEvent(String destination, String source,byte[] value,String registerId,int count,int size,int version){
        this.destination = destination;
        this.source = source;
        this.payload = value;
        this.trackId = registerId;
        this.stub = count;
        this.retries = size;
        this.version = version;
    }
    @Override
    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("2",this.destination);
        out.writeUTF("3",this.source);
        out.writeUTF("4",this.trackId);
        out.writeInt("5",this.stub);
        out.writeInt("6",this.retries);
        out.writeInt("7",this.version);
        out.writeByteArray("8",this.payload);
    }
    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.destination = in.readUTF("2");
        this.source = in.readUTF("3");
        this.trackId = in.readUTF("4");
        this.stub = in.readInt("5");
        this.retries = in.readInt("6");
        this.version = in.readInt("7");
        this.payload = in.readByteArray("8");
    }
    @Override
    public int getClassId() {
        return PortableEventRegistry.MAP_STORE_RECOVERY_EVENT_CID;
    }
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }
    @Override
    public String toString(){
        return "map store on recovery event to ->["+source+"/"+stub+"/"+retries+"/"+version+"/"+payload.length+"]";
    }
}
