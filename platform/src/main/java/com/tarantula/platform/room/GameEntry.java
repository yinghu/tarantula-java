package com.tarantula.platform.room;

import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Configurable;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;
import java.util.Map;

public class GameEntry extends RecoverableObject implements Configurable, Portable {
    public static final String LABEL = "GGE";
    public String systemId;
    public int seatIndex;
    public boolean occupied;
    public GameEntry(){
        this.label = LABEL;
        this.onEdge = true;
    }
    public GameEntry(int seatIndex){
        this();
        this.seatIndex = seatIndex;
    }
    @Override
    public Map<String,Object> toMap(){
        properties.put("1",seatIndex);
        properties.put("2",systemId);
        properties.put("3",occupied);
        return properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.seatIndex  =  ((Number)properties.getOrDefault("1",0)).intValue();
        this.systemId = (String)properties.getOrDefault("2",null);
        this.occupied = (boolean)properties.getOrDefault("3",false);
    }

    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PortableEventRegistry.GAME_ENTRY_CID;
    }

    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeInt("1",seatIndex);
        portableWriter.writeUTF("2",systemId);
        portableWriter.writeBoolean("3",occupied);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        seatIndex = portableReader.readInt("1");
        systemId = portableReader.readUTF("2");
        occupied = portableReader.readBoolean("3");
    }

    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("SystemId",systemId);
        jsonObject.addProperty("Seat",seatIndex+1);
        jsonObject.addProperty("Occupied",occupied);
        return jsonObject;
    }
}
