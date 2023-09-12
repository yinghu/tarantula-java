package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Event;
import com.icodesoftware.Recoverable;
import com.icodesoftware.lmdb.BufferProxy;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MapStoreSyncEvent extends Data implements Event {

    public int factoryId;
    public int classId;


    public ByteBuffer key = ByteBuffer.allocate(100);
    public ByteBuffer value = ByteBuffer.allocate(3000);

    public MapStoreSyncEvent(){

    }
    public MapStoreSyncEvent(Recoverable recoverable){
        factoryId = recoverable.getFactoryId();
        classId = recoverable.getClassId();
        recoverable.writeKey(new BufferProxy(key));
        key.flip();
        recoverable.write(new BufferProxy(value));
        value.flip();
    }
    @Override
    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1",this.destination);
        out.writeInt("2",factoryId);
        out.writeInt("3",classId);
        out.writeByteArray("4",key.array());
        out.writeByteArray("5",value.array());
    }
    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.destination = in.readUTF("1");
        this.factoryId = in.readInt("2");
        this.classId = in.readInt("3");
        key.put(in.readByteArray("4")).flip();
        value.put(in.readByteArray("5")).flip();
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
        return "Map store sync event ->["+factoryId+"/"+classId+"]";
    }
}
