package com.tarantula.platform.room;

import com.google.gson.JsonObject;

import com.icodesoftware.util.RecoverableObject;
import com.tarantula.game.GamePortableRegistry;


public class GameEntry extends RecoverableObject implements GameRoom.Entry{


    private long systemId;
    private long stub;
    private int seat;
    private boolean occupied;
    private int team;

    public GameEntry(){
        this.label = LABEL;
        this.onEdge = true;
    }
    public int number(){
        return seat;
    }
    public long systemId(){
        return systemId;
    }
    public long stub(){
        return stub;
    }
    public int team(){
        return team;
    }
    public boolean occupied(){
        return occupied;
    }

    public void number(int seat){
        this.seat = seat;
    }
    public void systemId(long systemId){
        this.systemId = systemId;
    }
    public void stub(long stub){
        this.stub = stub;
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
        this.systemId = buffer.readLong();
        this.stub = buffer.readLong();
        return true;
    }
    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeInt(seat);
        buffer.writeInt(team);
        buffer.writeBoolean(occupied);
        buffer.writeLong(systemId);
        buffer.writeLong(stub);
        return true;
    }

    @Override
    public int getFactoryId() {
        return GamePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return GamePortableRegistry.GAME_ENTRY_CID;
    }


    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("EntryId",distributionKey());
        jsonObject.addProperty("SystemId",systemId);
        jsonObject.addProperty("Stub",stub);
        jsonObject.addProperty("Seat",seat);
        jsonObject.addProperty("Team",team);
        jsonObject.addProperty("Occupied",occupied);
        return jsonObject;
    }

    public void reset(){
        this.occupied = false;
    }
}

