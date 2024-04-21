package com.tarantula.platform.presence.leaderboard;

import com.google.gson.JsonObject;
import com.icodesoftware.LeaderBoard;

import com.icodesoftware.util.OnApplicationHeader;
import com.tarantula.platform.presence.PresencePortableRegistry;


public class LeaderBoardEntry extends OnApplicationHeader implements LeaderBoard.Entry {

    private double value=0;
    private String classifier;
    private String category;

    private int rank;
    public LeaderBoardEntry(){
        this.onEdge = true;
        this.systemId = 0;
        this.value=0;
        this.timestamp=0;
    }
    //create entry
    public LeaderBoardEntry(String classifier,String category){
        this();
        this.classifier = classifier;
        this.category = category;
        label = classifier+"_"+category;
    }
    //update entry
    public LeaderBoardEntry(long systemId, double value, long timestamp){
        this();
        this.systemId = systemId;
        this.value = value;
        this.timestamp = timestamp;
    }

    public LeaderBoard.Entry update(LeaderBoard.Entry entry){
        this.systemId = entry.systemId();
        this.value = entry.value();
        this.timestamp = entry.timestamp();
        return this;
    }
    LeaderBoard.Entry reset(){
        this.systemId=0;
        this.value=0;
        this.timestamp=0;
        return this;
    }
    public String category(){
        return this.category;
    }
    public String classifier(){
        return this.classifier;
    }

    public double value() {
        return value;
    }

    public int rank(){
        return rank;
    }
    public void rank(int rank){
        this.rank = rank;
    }
    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PresencePortableRegistry.LEADER_BOARD_ENTRY_CID;
    }
    @Override
    public String toString(){
        return classifier+"/"+category+"/"+this.systemId+"/"+value+"/"+timestamp;
    }


    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeLong(systemId);
        buffer.writeDouble(value);
        buffer.writeLong(timestamp);
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        systemId = buffer.readLong();
        value = buffer.readDouble();
        timestamp = buffer.readLong();
        return true;
    }

    @Override
    public boolean equals(Object obj){
        LeaderBoardEntry lde = (LeaderBoardEntry)obj;
        return this.systemId==(lde.systemId());
    }
    @Override
    public int hashCode(){
        return Long.hashCode(systemId);
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Category",category);
        jsonObject.addProperty("Classifier",classifier);
        jsonObject.addProperty("Rank",rank);
        jsonObject.addProperty("SystemId",systemId);
        jsonObject.addProperty("Value",value);
        jsonObject.addProperty("Timestamp",timestamp);
        return jsonObject;
    }
}
