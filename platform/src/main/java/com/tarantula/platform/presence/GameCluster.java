package com.tarantula.platform.presence;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.platform.OnAccessTrack;
import com.tarantula.platform.event.PortableEventRegistry;


import java.io.IOException;
import java.util.Map;

public class GameCluster extends OnAccessTrack implements Portable {

    @Override
    public Map<String,Object> toMap(){
        properties.put("1",accessKey);
        return properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        accessKey = (String) properties.get("1");
    }

    @Override
    public int getClassId() {
        return PortableEventRegistry.GAME_CLUSTER_CID;
    }

    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeUTF("1",name);
        portableWriter.writeBoolean("2",disabled);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        name = portableReader.readUTF("1");
        disabled = portableReader.readBoolean("2");
    }

    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }
}
