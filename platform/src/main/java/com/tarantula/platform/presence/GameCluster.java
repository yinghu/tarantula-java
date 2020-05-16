package com.tarantula.platform.presence;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.platform.DefaultDescriptor;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;
import java.util.Map;

public class GameCluster extends DefaultDescriptor implements Portable {

    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {

    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {

    }
    @Override
    public Map<String,Object> toMap(){
        properties.put("typeId",typeId);
        //properties.put()
        return properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){

    }

    @Override
    public int getClassId() {
        return PortableEventRegistry.GAME_CLUSTER_CID;
    }
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }
}
