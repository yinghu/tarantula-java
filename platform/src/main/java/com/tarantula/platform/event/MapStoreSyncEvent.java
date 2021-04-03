package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Event;

import java.io.IOException;

public class MapStoreSyncEvent extends Data implements Event {


    public MapStoreSyncEvent(){

    }
    public MapStoreSyncEvent(String destination,String systemId,int factoryId,int classId,String key,byte[] value){
        this.destination = destination;
        this.systemId = systemId;
        this.accessMode = factoryId;
        this.stub = classId;
        this.index = key;
        this.payload = value;
    }
    @Override
    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1",this.destination);
        out.writeUTF("2",this.systemId);
        out.writeInt("3",accessMode);
        out.writeInt("4",stub);
        out.writeUTF("5",this.index);
        out.writeByteArray("6",this.payload);
    }
    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.destination = in.readUTF("1");
        this.systemId = in.readUTF("2");
        this.accessMode = in.readInt("3");
        this.stub = in.readInt("4");
        this.index = in.readUTF("5");
        this.payload = in.readByteArray("6");
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
        return "Map store sync event ->["+systemId+"/"+index+">>>"+new String(payload)+"]";
    }
}
