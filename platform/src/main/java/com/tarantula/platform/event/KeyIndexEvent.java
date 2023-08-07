package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Event;
import com.icodesoftware.service.KeyIndexService;

import java.io.IOException;

public class KeyIndexEvent extends Data implements Event {

    public KeyIndexEvent(){}

    public KeyIndexEvent(String owner,String key,String master,String slave){
        this.owner = owner;
        this.index = key;
        this.source = master;
        this.label = slave;
    }
    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeUTF("1",owner);
        portableWriter.writeUTF("2",index);
        portableWriter.writeUTF("3",source);
        portableWriter.writeUTF("4",label);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        owner = portableReader.readUTF("1");
        index = portableReader.readUTF("2");
        source = portableReader.readUTF("3");
        label = portableReader.readUTF("4");
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
        return "Key ["+owner+"_"+index+"] on ["+label+"]";
    }
}
