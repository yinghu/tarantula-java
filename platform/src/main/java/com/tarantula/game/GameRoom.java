package com.tarantula.game;

import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Tournament;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;
import java.util.Map;

public class GameRoom extends RecoverableObject implements Portable {

    public boolean offline;
    public Tournament.Instance instance;

    public GameRoom(){

    }
    public GameRoom(boolean offline){
        this.offline = offline;
    }
    public String roomId(){
        return this.distributionKey();
    }
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",offline);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.offline = (boolean)properties.get("1");
    }
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PortableEventRegistry.ROOM_CID;
    }

    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeUTF("1",this.distributionKey());
        portableWriter.writeBoolean("2",offline);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        this.distributionKey(portableReader.readUTF("1"));
        this.offline = portableReader.readBoolean("2");
    }
    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("roomId",distributionKey());
        jsonObject.addProperty("offline",offline);
        return jsonObject;
    }
}