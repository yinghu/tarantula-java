package com.tarantula.platform.presence.leaderboard;

import com.google.gson.JsonObject;
import com.icodesoftware.LeaderBoard;

import com.icodesoftware.Statistics;
import com.icodesoftware.lmdb.BufferProxy;
import com.icodesoftware.util.OnApplicationHeader;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.util.ArrayList;
import java.util.List;


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
    private LeaderBoardEntry(String classifier,String category,long systemId,double value){
        this();
        this.classifier = classifier;
        this.category = category;
        this.systemId = systemId;
        this.value = value;
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

    public List<LeaderBoardEntry> board(){
        ArrayList<LeaderBoardEntry> board = new ArrayList<>();

        return board;
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
    public void fromBinary(byte[] payload) {
        DataBuffer buffer = BufferProxy.wrap(payload);
        systemId = buffer.readLong();
        value = buffer.readDouble();
        timestamp = buffer.readLong();
    }

    @Override
    public byte[] toBinary() {
        DataBuffer buffer = BufferProxy.buffer(24,false);
        buffer.writeLong(systemId);
        buffer.writeDouble(value);
        buffer.writeLong(timestamp);
        return buffer.array();
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
        jsonObject.addProperty("Rank",rank);
        jsonObject.addProperty("SystemId",systemId);
        jsonObject.addProperty("Value",value);
        jsonObject.addProperty("Timestamp",timestamp);
        return jsonObject;
    }
    public static LeaderBoard.Entry[] from(Statistics.Entry statisticsEntry){
        LeaderBoard.Entry[] entries = new LeaderBoardEntry[5];
        long stamp = System.currentTimeMillis();
        entries[0] = from(LeaderBoard.DAILY,statisticsEntry.name(),statisticsEntry.systemId(),statisticsEntry.daily(),stamp);
        entries[1] = from(LeaderBoard.WEEKLY,statisticsEntry.name(),statisticsEntry.systemId(),statisticsEntry.weekly(),stamp);
        entries[2] = from(LeaderBoard.MONTHLY,statisticsEntry.name(),statisticsEntry.systemId(),statisticsEntry.monthly(),stamp);
        entries[3] = from(LeaderBoard.YEARLY,statisticsEntry.name(),statisticsEntry.systemId(),statisticsEntry.yearly(),stamp);
        entries[4] = from(LeaderBoard.TOTAL,statisticsEntry.name(),statisticsEntry.systemId(),statisticsEntry.total(),stamp);
        return entries;
    }
    public static LeaderBoard.Entry from(String classifier,String category,long systemId,double value,long timestamp){
        LeaderBoard.Entry entry = new LeaderBoardEntry(classifier,category,systemId,value);
        entry.timestamp(timestamp);
        return entry;
    }

    public LeaderBoard.Entry duplicate(int rank){
        LeaderBoard.Entry copy = LeaderBoardEntry.from(classifier,category,systemId,value,timestamp);
        copy.rank(rank);
        return copy;
    }
}
