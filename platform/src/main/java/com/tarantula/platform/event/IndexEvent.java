package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.Event;
import com.tarantula.platform.Data;

import java.io.IOException;

/**
 * Updated by yinghu on 4/28//2018.
 */
public class IndexEvent extends Data implements Event {

    public String userAgent;

    public String viewId;
    public String flag;

    public IndexEvent(){}

    public IndexEvent(String source,String sessionId){
        this.source = source;
        this.sessionId = sessionId;
    }
    @Override
    public int getFactoryId(){
        return PortableEventRegistry.OID;
    }
    @Override
    public int getClassId(){
        return PortableEventRegistry.INDEX_EVENT_CID;
    }
    @Override
    public void writePortable(PortableWriter out) throws IOException {
        //out.writeUTF("f1",this.eventId);
        out.writeUTF("f3",this.source);
        out.writeUTF("f5",this.sessionId);
        out.writeUTF("d1",this.userAgent);
        out.writeUTF("d7",this.viewId);
        out.writeUTF("d8",this.action);
        out.writeUTF("d9",this.flag);
        out.writeUTF("d10",this.destination);
    }
    @Override
    public void readPortable(PortableReader in) throws IOException {
        //this.eventId = in.readUTF("f1");
        this.source = in.readUTF("f3");
        this.sessionId = in.readUTF("f5");
        this.userAgent = in.readUTF("d1");
        this.viewId = in.readUTF("d7");
        this.action = in.readUTF("d8");
        this.flag = in.readUTF("d9");
        this.destination = in.readUTF("d10");
    }

    @Override
    public String toString(){
        return "Index Event ["+this.destination+"]";
    }
}
