package com.tarantula.platform.tournament;

import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Tournament;

import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;
import java.time.LocalDateTime;


public class TournamentEntry extends RecoverableObject implements Tournament.Entry, Portable {

    private String systemId;
    private double credits;
    private double score;
    private boolean finished;
    private int rank;

    public TournamentEntry(String systemId,double credits){
        this();
        this.systemId = systemId;
        this.credits = credits;
    }
    public TournamentEntry(){
        this.onEdge = true;
        this.label = Tournament.ENTRY_LABEL;
    }
    @Override
    public String systemId() {
        return systemId;
    }

    @Override
    public void score(double credit,double delta) {
        if(credits - credit < 0) return;
        score += delta;
        credits -= credit;
        timestamp = TimeUtil.toUTCMilliseconds(LocalDateTime.now());
        finished = credits <= 0;
        this.update();
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
    void rank(int rank){
        this.rank = rank;
    }


    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeUTF8(systemId);
        buffer.writeDouble(score);
        buffer.writeLong(timestamp);
        buffer.writeInt(rank);
        buffer.writeDouble(credits);
        buffer.writeBoolean(finished);
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        systemId = buffer.readUTF8();
        score = buffer.readDouble();
        timestamp = buffer.readLong();
        rank = buffer.readInt();
        credits = buffer.readDouble();
        finished = buffer.readBoolean();
        return true;
    }

    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableEventRegistry.TOURNAMENT_ENTRY_CID;
    }

    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeUTF("1",systemId);
        portableWriter.writeDouble("2",score);
        portableWriter.writeLong("3",timestamp);
        portableWriter.writeInt("4",rank);
        portableWriter.writeDouble("5",credits);
        portableWriter.writeBoolean("6",finished);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        this.systemId = portableReader.readUTF("1");
        this.score = portableReader.readDouble("2");
        this.timestamp = portableReader.readLong("3");
        this.rank = portableReader.readInt("4");
        this.credits = portableReader.readDouble("5");
        this.finished = portableReader.readBoolean("6");
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("systemId",systemId);
        jsonObject.addProperty("credits",credits);
        jsonObject.addProperty("score",score);
        jsonObject.addProperty("rank",rank);
        jsonObject.addProperty("timestamp",timestamp);
        jsonObject.addProperty("finished",finished);
        return jsonObject;
    }
}
