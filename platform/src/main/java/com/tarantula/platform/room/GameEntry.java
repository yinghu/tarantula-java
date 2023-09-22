package com.tarantula.platform.room;

import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;
import java.util.Map;

public class GameEntry extends RecoverableObject implements GameRoom.Entry{


    private long stubId;
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
    public long stubId(){
        return stubId;
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
    public void stubId(long stubId){
        this.stubId = stubId;
    }
    public void team(int team){
        this.team = team;
    }
    public void occupied(boolean occupied){
        this.occupied = occupied;
    }



    @Override
    public boolean read(DataBuffer buffer){
        this.seat = buffer.readInt();
        this.team = buffer.readInt();
        this.occupied = buffer.readBoolean();
        this.stubId = buffer.readLong();
        return true;
    }
    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeInt(seat);
        buffer.writeInt(team);
        buffer.writeBoolean(occupied);
        buffer.writeLong(stubId);
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
        portableWriter.writeLong("3",stubId);
        portableWriter.writeBoolean("4",occupied);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        seat = portableReader.readInt("1");
        team = portableReader.readInt("2");
        stubId = portableReader.readLong("3");
        occupied = portableReader.readBoolean("4");
    }

    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("EntryId",distributionKey());
        jsonObject.addProperty("StubId",stubId);
        jsonObject.addProperty("Seat",seat);
        jsonObject.addProperty("Team",team);
        jsonObject.addProperty("Occupied",occupied);
        return jsonObject;
    }

    public void reset(){
        this.occupied = false;
    }
}
