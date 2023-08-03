package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Event;
import com.icodesoftware.service.KeyIndexService;

import java.io.IOException;

public class KeyIndexEvent extends Data implements Event {

    public KeyIndexEvent(){}

    public KeyIndexEvent(String key,String node){
        this.index = key;
        this.label = node;
    }
    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeUTF("1",index);
        portableWriter.writeUTF("2",label);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        index = portableReader.readUTF("1");
        label = portableReader.readUTF("2");
    }

    @Override
    public int getClassId() {
        return PortableEventRegistry.KEY_INDEX_EVENT_CID;
    }
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }

    public String destination() {
        return KeyIndexService.NAME;
    }

    @Override
    public String toString(){
        return "Key ["+index+"] on ["+label+"]";
    }
}
