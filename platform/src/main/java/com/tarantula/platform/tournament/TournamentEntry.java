package com.tarantula.platform.tournament;

import com.google.gson.JsonObject;

import com.icodesoftware.Tournament;

import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.event.PortableEventRegistry;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class TournamentEntry extends RecoverableObject implements Tournament.Entry{

    private long systemId;
    private double credits;
    private double score;
    private boolean finished;
    private int rank;

    public TournamentEntry(long systemId,double score, int rank){
        this();
        this.systemId = systemId;
        this.score = score;
        this.rank = rank;
    }
    public TournamentEntry(long systemId,double credits,double score){
        this();
        this.systemId = systemId;
        this.credits = credits;
        this.score = score;
    }
    public TournamentEntry(){
        this.onEdge = true;
        this.label = Tournament.ENTRY_LABEL;
    }

    private TournamentEntry(long systemId,double score,long timestamp, int rank){
        this();
        this.systemId = systemId;
        this.score = score;
        this.timestamp = timestamp;
        this.rank = rank;
    }
    @Override
    public long systemId() {
        return systemId;
    }

    @Override
    public double score(double credit,double delta) {
        if(credits - credit < 0) return 0;
        score += delta;
        credits -= credit;
        timestamp = TimeUtil.toUTCMilliseconds(LocalDateTime.now());
        finished = credits <= 0;
        this.update();
        return score;
    }

    public double score(){
        return score;
    }
    public void finish(){
        finished = true;
    }
    public boolean finished(){
        return finished;
    }
    public double credit(){
        return credits;
    }
    @Override
    public int rank(){
        return rank;
    }
    public void rank(int rank){
        this.rank = rank;
    }


    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeLong(systemId);
        buffer.writeDouble(score);
        buffer.writeLong(timestamp);
        buffer.writeInt(rank);
        buffer.writeDouble(credits);
        buffer.writeBoolean(finished);
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        systemId = buffer.readLong();
        score = buffer.readDouble();
        timestamp = buffer.readLong();
        rank = buffer.readInt();
        credits = buffer.readDouble();
        finished = buffer.readBoolean();
        return true;
    }

    @Override
    public int getFactoryId() {
        return TournamentPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return TournamentPortableRegistry.TOURNAMENT_ENTRY_CID;
    }



    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("SystemId",Long.toString(systemId));
        jsonObject.addProperty("Credits",credits);
        jsonObject.addProperty("Score",Double.valueOf(score).intValue());
        jsonObject.addProperty("Rank",rank);
        jsonObject.addProperty("LastUpdated",TimeUtil.fromUTCMilliseconds(timestamp).format(DateTimeFormatter.ISO_DATE_TIME));
        //jsonObject.addProperty("Finished",finished);
        return jsonObject;
    }

    public void update(TournamentEntry pendingEntry){
        this.systemId = pendingEntry.systemId();
        this.score = pendingEntry.score();
        this.timestamp = pendingEntry.timestamp;
    }

    public TournamentEntry duplicate(int rank){
        return new TournamentEntry(systemId,score,timestamp,rank);
    }
    public static TournamentEntry from(long systemId,double score,long timestamp,int rank){
        return new TournamentEntry(systemId,score,timestamp,rank);
    }
}
