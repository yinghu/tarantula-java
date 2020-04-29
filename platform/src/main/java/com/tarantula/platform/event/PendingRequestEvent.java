package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;

import java.io.IOException;

/**
 * Updated by yinghu lu on 7/1/2018.
 */
public class PendingRequestEvent extends Data implements EventOnAction {

    public PendingRequestEvent(){
    }
    public PendingRequestEvent(String tag){
        this.action = tag;
    }
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }
    public int getClassId() {
        return PortableEventRegistry.PENDING_REQUEST_EVENT_CID;
    }

    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1",this.destination);
        out.writeUTF("2",this.source);
        out.writeUTF("3",this.sessionId);
        out.writeUTF("4",this.systemId);
        out.writeUTF("5",this.ticket);
        out.writeInt("6",this.stub);
        out.writeUTF("7",this.trackId);
        out.writeUTF("8",this.action);
        //out.writeUTF("9",this.tag);
    }

    public void readPortable(PortableReader in) throws IOException {
        this.destination = in.readUTF("1");
        this.source = in.readUTF("2");
        this.sessionId = in.readUTF("3");
        this.systemId = in.readUTF("4");
        this.ticket = in.readUTF("5");
        this.stub = in.readInt("6");
        this.trackId = in.readUTF("7");
        this.action = in.readUTF("8");
        //this.tag = in.readUTF("9");
    }
    @Override
    public String toString(){
        return "Pending Request Event On ["+destination+"]";
    }
}
