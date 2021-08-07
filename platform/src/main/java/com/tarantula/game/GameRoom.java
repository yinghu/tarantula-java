package com.tarantula.game;

import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Tournament;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;

public class GameRoom extends RecoverableObject implements Portable {

    public String roomId;
    public boolean offline;
    public Tournament.Instance instance;

    public GameRoom(){

    }
    public GameRoom(boolean offline){
        this.offline = offline;
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
        portableWriter.writeUTF("1",roomId);
        portableWriter.writeBoolean("2",offline);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        this.roomId = portableReader.readUTF("1");
        this.offline = portableReader.readBoolean("2");
    }
    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("roomId",roomId);
        jsonObject.addProperty("offline",offline);
        return jsonObject;
    }
}