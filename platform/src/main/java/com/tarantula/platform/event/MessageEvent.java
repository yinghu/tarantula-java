package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.Event;
import com.tarantula.platform.Data;

import java.io.IOException;

/**
 * Created by yinghu lu on 8/9/2019.
 */
public class MessageEvent extends Data implements Event {


    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PortableEventRegistry.MESSAGE_EVENT_CID;
    }

    public MessageEvent(){

    }
    public MessageEvent(String from,String to,byte[] payload){
        this.source = from;
        this.destination = to;
        this.payload = payload;
    }

    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1",this.destination);
        out.writeUTF("2",this.source);
        out.writeByteArray("3",payload);
    }

    public void readPortable(PortableReader in) throws IOException {
        this.destination = in.readUTF("1");
        this.source = in.readUTF("2");
        this.payload = in.readByteArray("3");
    }

    @Override
    public String toString(){
        return "Message Event from ["+source+"] to ["+destination+"]";
    }
}
