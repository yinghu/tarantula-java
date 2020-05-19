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
        properties.put("1",name);
        properties.put("2",accessKey);
        return properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        name = (String) properties.get("1");
        accessKey = (String) properties.get("2");
    }

    @Override
    public int getClassId() {
        return PortableEventRegistry.GAME_CLUSTER_CID;
    }

    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeUTF("1",name);
        portableWriter.writeBoolean("2",disabled);
        portableWriter.writeUTF("3",this.bucket);
        portableWriter.writeUTF("4",oid);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        name = portableReader.readUTF("1");
        disabled = portableReader.readBoolean("2");
        bucket = portableReader.readUTF("3");
        oid = portableReader.readUTF("4");
    }

    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }
}
