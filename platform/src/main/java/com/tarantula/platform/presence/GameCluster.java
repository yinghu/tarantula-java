package com.tarantula.platform.presence;

import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.platform.OnApplicationHeader;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;

public class GameCluster extends OnApplicationHeader implements Portable {

    public final static String NAME="1";
    public final static String PLAN="2";
    public final static String GAME_LOBBY = "3";
    public final static String GAME_SERVICE = "4";
    public final static String GAME_DATA = "5";
    public final static String OWNER = "6";
    public final static String ACCESS_KEY = "7";
    public final static String TIMESTAMP = "8";
    public final static String DISABLED = "9";

    public final static String LOBBY_LIST = "10";
    public final static String SERVICE_LIST = "11";
    public final static String DATA_LIST = "12";

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
            portableWriter.writeUTFArray(LOBBY_LIST,(String[])properties.get(LOBBY_LIST));
            portableWriter.writeUTFArray(SERVICE_LIST,(String[])properties.get(SERVICE_LIST));
            portableWriter.writeUTFArray(DATA_LIST,(String[])properties.get(DATA_LIST));
        }
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        successful = portableReader.readBoolean("1");
        message = portableReader.readUTF("2");
        if(successful){
            bucket = portableReader.readUTF("3");
            oid = portableReader.readUTF("4");
            properties.put(LOBBY_LIST,portableReader.readUTFArray(LOBBY_LIST));
            properties.put(SERVICE_LIST,portableReader.readUTFArray(SERVICE_LIST));
            properties.put(DATA_LIST,portableReader.readUTFArray(DATA_LIST));
        }
    }
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("successful",successful);
        if(successful){
            jsonObject.addProperty("accessId",this.distributionKey());
            jsonObject.addProperty("stub",this.stub);
        }else{
            jsonObject.addProperty("message",message);
        }
        return jsonObject;
    }

    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }
}
