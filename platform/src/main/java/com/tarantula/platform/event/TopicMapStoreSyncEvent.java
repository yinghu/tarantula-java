package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Event;

import java.io.IOException;

public class TopicMapStoreSyncEvent extends Data implements Event {

    private int factoryId;
    private int classId;

    public TopicMapStoreSyncEvent(){

    }
    public TopicMapStoreSyncEvent(String destination,byte[] value){
        this.destination = destination;
        this.payload = value;
    }
    public TopicMapStoreSyncEvent(String destination,int factoryId, int classId, String key, byte[] value){
        this.destination = destination;
        this.factoryId = factoryId;
        this.classId = classId;
        this.index = key;
        this.payload = value;
    }
    @Override
    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1",this.destination);
        out.writeInt("2",factoryId);
        out.writeInt("3",classId);
        out.writeUTF("4",this.index);
        out.writeByteArray("5",this.payload);
    }
    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.destination = in.readUTF("1");
        this.factoryId = in.readInt("2");
        this.classId = in.readInt("3");
        this.index = in.readUTF("4");
        this.payload = in.readByteArray("5");
    }
    @Override
    public int getClassId() {
        return PortableEventRegistry.TOPIC_MAP_STORE_SYNC_EVENT_CID;
    }
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }
    @Override
    public String toString(){
        return "Topic map store sync event ->["+destination+"/"+index+">>>"+new String(payload)+"]";
    }
}
