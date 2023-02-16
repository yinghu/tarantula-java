package com.tarantula.platform.tournament;

import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Tournament;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

public class TournamentEntry extends RecoverableObject implements Tournament.Entry, Portable {

    private String systemId;
    private double credits;
    private double score;
    private int rank;
    private JsonObject payload = new JsonObject();

    public TournamentEntry(String systemId,String instanceId,double credits){
        this();
        this.systemId = systemId;
        this.owner = instanceId;
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
    public double score(double credit,double delta) {
        score = score+delta;
        credits -= credit;
        if(delta>0){
            timestamp = TimeUtil.toUTCMilliseconds(LocalDateTime.now());
            this.update();
        }
        return score;
    }
    @Override
    public int rank(){
        return rank;
    }
    void rank(int rank){
        this.rank = rank;
    }
    public Map<String,Object> toMap(){
        properties.put("1",systemId);
        properties.put("2",score);
        properties.put("3",timestamp);
        properties.put("4",rank);
        properties.put("5",credits);
        return properties;
    }
    public void fromMap(Map<String,Object> properties){
        this.systemId = (String) properties.get("1");
        this.score = ((Number)properties.getOrDefault("2",0)).doubleValue();
        this.timestamp = ((Number)properties.getOrDefault("3",0)).longValue();
        this.rank = ((Number)properties.getOrDefault("4",0)).intValue();
        this.credits = ((Number)properties.getOrDefault("5",0)).doubleValue();

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
        //portableWriter.writeUTF("5",payload.toString());
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        this.systemId = portableReader.readUTF("1");
        this.score = portableReader.readDouble("2");
        this.timestamp = portableReader.readLong("3");
        this.rank = portableReader.readInt("4");
        //this.payload = JsonUtil.parse(portableReader.readUTF("5"));
    }

    @Override
    public boolean configureAndValidate(byte[] data){
        payload = JsonUtil.parse(data);
        return true;
    }
    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("systemId",systemId);
        jsonObject.addProperty("credits",credits);
        jsonObject.addProperty("score",score);
        jsonObject.addProperty("rank",rank);
        jsonObject.addProperty("timestamp",timestamp);
        return jsonObject;
    }
}
