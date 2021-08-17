package com.tarantula.game;

import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Module;
import com.icodesoftware.Tournament;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;
import java.util.Map;

public class GameRoom extends RecoverableObject implements Portable {

    public boolean offline;
    public int totalJoined;
    public boolean tournamentEnabled;
    public Arena arena;
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
        this.properties.put("2",totalJoined);
        this.properties.put("3",tournamentEnabled);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.offline = (boolean)properties.getOrDefault("1",true);
        this.totalJoined = ((Number)properties.getOrDefault("2",0)).intValue();
        this.tournamentEnabled = (boolean)properties.getOrDefault("3",false);
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
        portableWriter.writeInt("3",totalJoined);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        this.distributionKey(portableReader.readUTF("1"));
        this.offline = portableReader.readBoolean("2");
        this.totalJoined = portableReader.readInt("3");
    }
    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("roomId",distributionKey());
        jsonObject.addProperty("offline",offline);
        jsonObject.addProperty("totalJoined",totalJoined);
        jsonObject.addProperty("tournamentEnabled",tournamentEnabled);
        return jsonObject;
    }
    public void onTimer(Module.OnUpdate onUpdate){
        onUpdate.on(null,"label",new byte[0]);
    }
}