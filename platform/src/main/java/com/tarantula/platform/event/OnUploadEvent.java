package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.Event;


import java.io.IOException;

public class OnUploadEvent extends Data implements Event {

    public OnUploadEvent(){}
    public OnUploadEvent(String dest, String source,String trackId,byte[] payload){
        this.destination =dest;
        this.source = source;
        this.trackId = trackId;
        this.payload = payload;
    }

    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeUTF("1",this.destination);
        portableWriter.writeUTF("2",this.source);
        portableWriter.writeUTF("3",this.trackId);
        portableWriter.writeByteArray("4",payload);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        this.destination = portableReader.readUTF("1");
        this.source = portableReader.readUTF("2");
        this.trackId = portableReader.readUTF("3");
        this.payload = portableReader.readByteArray("4");
    }
    @Override
    public int getClassId() {
        return PortableEventRegistry.ON_UPLOAD_EVENT_CID;
    }
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }

}
