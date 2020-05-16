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
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeUTF("1",typeId);
        portableWriter.writeUTF("2",name);
        //portableWriter.writeUTF("3",description);
        //portableWriter.writeBoolean("4",singleton);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        this.typeId = portableReader.readUTF("1");
        this.name = portableReader.readUTF("2");
        //this.description = portableReader.readUTF("3");
        //this.singleton = portableReader.readBoolean("4");
    }
    @Override
    public Map<String,Object> toMap(){
        properties.put("1",typeId);
        properties.put("2",name);
        //properties.put("3",this.description);
        //properties.put("4",this.singleton);
        return properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        typeId = (String) properties.get("1");
        name = (String) properties.get("2");
        //description = (String) properties.get("3");
        //singleton = (boolean) properties.get("4");
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
