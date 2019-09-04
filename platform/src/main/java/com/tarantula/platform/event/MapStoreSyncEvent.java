package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.Event;
import com.tarantula.platform.service.persistence.RecoverableMetadata;

import java.io.IOException;

/**
 * Updated by yinghu lu on 4/7/2019.
 */
public class MapStoreSyncEvent extends Data implements Event {

    public byte[] key;
    public RecoverableMetadata metadata;

    public MapStoreSyncEvent(){
        this.forwarding = true;
    }
    public MapStoreSyncEvent(String destination, String source,byte[] key,byte[] value, RecoverableMetadata metadata){
        this();
        this.destination = destination;
        this.source = source;
        this.key = key;
        this.payload = value;
        this.metadata = metadata;
    }
    public MapStoreSyncEvent(String destination, String source,String systemId,byte[] key,byte[] value, RecoverableMetadata metadata){
        this();
        this.destination = destination;
        this.source = source;
        this.systemId = systemId;
        this.key = key;
        this.payload = value;
        this.metadata = metadata;
    }
    @Override
    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1",this.source);
        out.writeUTF("2",this.destination);
        out.writeByteArray("3",this.key);
        out.writeByteArray("4",this.payload);
        out.writePortable("5",this.metadata);
        out.writeUTF("6",this.systemId);
        out.writeUTF("7",this.trackId);
    }
    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.source = in.readUTF("1");
        this.destination = in.readUTF("2");
        this.key = in.readByteArray("3");
        this.payload = in.readByteArray("4");
        this.metadata = in.readPortable("5");
        this.systemId = in.readUTF("6");
        this.trackId = in.readUTF("7");
    }
    @Override
    public int getClassId() {
        return PortableEventRegistry.MAP_STORE_SYNC_EVENT_CID;
    }
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }
    @Override
    public String toString(){
        return "Map store sync event ->["+metadata.toString()+"/"+destination+"/"+source+"/"+systemId+"]";
    }
}
