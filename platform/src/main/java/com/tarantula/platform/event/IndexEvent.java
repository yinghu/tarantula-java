package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.EventOnAction;

import java.io.IOException;

/**
 * Updated by yinghu on 9/3/2019
 */
public class IndexEvent extends Data implements EventOnAction {


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
        out.writeUTF("1",this.source);
        out.writeUTF("2",this.sessionId);
        out.writeUTF("3",this.trackId);
        out.writeUTF("4",this.action);
        out.writeUTF("5",this.destination);
    }
    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.source = in.readUTF("1");
        this.sessionId = in.readUTF("2");
        this.trackId = in.readUTF("3");
        this.action = in.readUTF("4");
        this.destination = in.readUTF("5");
    }

    @Override
    public String toString(){
        return "Index Event ["+this.destination+"/"+action+"]";
    }
}
