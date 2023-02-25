package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Event;

import java.io.IOException;

public class ServerPushEvent extends Data implements Event {


    public ServerPushEvent(){

    }

    public ServerPushEvent(String destination,int stub,byte[] value){
        this.destination = destination;
        this.stub = stub;
        this.payload = value;
    }
    @Override
    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1",this.destination);
        out.writeInt("2",this.stub);
        out.writeByteArray("3",this.payload);
    }
    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.destination = in.readUTF("1");
        this.stub = in.readInt("2");
        this.payload = in.readByteArray("3");
    }
    @Override
    public int getClassId() {
        return PortableEventRegistry.SERVER_PUSH_EVENT_CID;
    }
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }
    @Override
    public String toString(){
        return "Server push event ->["+destination+"/"+stub+">>>"+new String(payload)+"]";
    }
}
