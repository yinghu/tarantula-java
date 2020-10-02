package com.tarantula.platform;

import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;

public class GameCluster extends OnApplicationHeader implements Portable {

    public final static String NAME="1";
    public final static String GAME_LOBBY = "3";
    public final static String GAME_SERVICE = "4";
    public final static String GAME_DATA = "5";
    public final static String OWNER = "6";
    public final static String ACCESS_KEY = "7";
    public final static String TIMESTAMP = "8";
    public final static String DISABLED = "9";


    @Override
    public int getClassId() {
       return PortableEventRegistry.GAME_CLUSTER_CID;
    }

    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeBoolean("1",successful);
        portableWriter.writeUTF("2",message);
        if(successful){
            portableWriter.writeUTF("3",this.bucket);
            portableWriter.writeUTF("4",oid);
        }
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        successful = portableReader.readBoolean("1");
        message = portableReader.readUTF("2");
        if(successful){
            bucket = portableReader.readUTF("3");
            oid = portableReader.readUTF("4");
        }
    }
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("successful",successful);
        jsonObject.addProperty("message",message);
        if(successful){
            jsonObject.addProperty("accessId",this.distributionKey());
        }
        return jsonObject;
    }

    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }
}
