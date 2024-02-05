package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;

public class KeyValueSet implements Portable {

    public byte[] key;
    public byte[] value;

    public KeyValueSet(){

    }

    public KeyValueSet(byte[] key,byte[] value){
        this.key = key;
        this.value = value;
    }
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableEventRegistry.KEY_VALUE_SET_CID;
    }

    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeByteArray("key",key);
        portableWriter.writeByteArray("value",value);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        key = portableReader.readByteArray("key");
        value = portableReader.readByteArray("value");
    }
}
