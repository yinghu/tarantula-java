package com.tarantula.platform.room;

import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;
import java.util.Map;

public class GameEntry extends RecoverableObject implements GameRoom.Entry{


    private String systemId;
    private int seat;
    private boolean occupied;
    private int team;

    public GameEntry(){
        this.label = LABEL;
        this.onEdge = true;
    }
    public int seat(){
        return seat;
    }
    public String systemId(){
        return systemId;
    }
    public int team(){
        return team;
    }
    public boolean occupied(){
        return occupied;
    }

    public void seat(int seat){
        this.seat = seat;
    }
    public void systemId(String systemId){
        this.systemId = systemId;
    }
    public void team(int team){
        this.team = team;
    }
    public void occupied(boolean occupied){
        this.occupied = occupied;
    }

    @Override
    public Map<String,Object> toMap(){
        properties.put("1",seat);
        properties.put("2",team);
        properties.put("3",systemId);
        properties.put("4",occupied);
        return properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.seat  =  ((Number)properties.getOrDefault("1",0)).intValue();
        this.team = ((Number)properties.getOrDefault("2",0)).intValue();
        this.systemId = (String)properties.getOrDefault("3",null);
        this.occupied = (boolean)properties.getOrDefault("4",false);
    }

    @Override
    public boolean read(DataBuffer buffer){
        this.seat = buffer.readInt();
        this.team = buffer.readInt();
        this.occupied = buffer.readBoolean();
        //this.totalJoined = buffer.readInt();
        //this.totalLeft = buffer.readInt();
        return true;
    }
    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeInt(seat);
        buffer.writeInt(team);
        buffer.writeBoolean(occupied);
        //buffer.writeInt(totalLeft);
        return true;
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
        portableWriter.writeInt("1",seat);
        portableWriter.writeInt("2",team);
        portableWriter.writeUTF("3",systemId);
        portableWriter.writeBoolean("4",occupied);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        seat = portableReader.readInt("1");
        team = portableReader.readInt("2");
        systemId = portableReader.readUTF("3");
        occupied = portableReader.readBoolean("4");
    }

    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("EntryId",distributionKey());
        jsonObject.addProperty("SystemId",systemId);
        jsonObject.addProperty("Seat",seat);
        jsonObject.addProperty("Team",team);
        jsonObject.addProperty("Occupied",occupied);
        return jsonObject;
    }

    public void reset(){
        this.occupied = false;
    }
}
