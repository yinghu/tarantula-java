package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.Event;
import com.tarantula.OnView;


import java.io.IOException;

public class OnViewEvent extends Data implements Event {

    public OnViewEvent(){}
    public OnViewEvent(String dest, String source, OnView onViewTrack){
        this.destination =dest;
        this.source = source;
        this.portable = (Portable) onViewTrack;
    }

    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeUTF("1",this.destination);
        portableWriter.writeUTF("2",this.source);
        portableWriter.writePortable("3",portable);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        this.destination = portableReader.readUTF("1");
        this.source = portableReader.readUTF("2");
        this.portable = portableReader.readPortable("3");
    }
    @Override
    public int getClassId() {
        return PortableEventRegistry.ON_VIEW_EVENT_CID;
    }
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }

}
