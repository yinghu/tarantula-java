package com.tarantula.platform.service;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Distributable;
import com.icodesoftware.service.KeyIndex;
import com.icodesoftware.util.NaturalKey;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;
import java.util.Map;

public class KeyIndexTrack extends RecoverableObject implements KeyIndex , Portable {

    @Override
    public boolean created() {
        return disabled;
    }


    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeBoolean("1",disabled);
        portableWriter.writeUTF("2",owner);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        disabled = portableReader.readBoolean("1");
        owner = portableReader.readUTF("2");
    }

    public int scope(){
        return Distributable.LOCAL_SCOPE;
    }
    @Override
    public boolean backup(){
        return false;
    }
    @Override
    public boolean distributable(){return true;}
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableEventRegistry.KEY_INDEX_CID;
    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("2",owner);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.owner = (String)properties.get("1");
    }

    public Key key(){
        return new NaturalKey(this.index);
    }
}
