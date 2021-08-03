package com.tarantula.platform.tournament;

import com.google.gson.JsonObject;
import com.icodesoftware.Tournament;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.RecoverableObject;

import java.util.Map;

public class TournamentEntry extends RecoverableObject implements Tournament.Entry {

    private String systemId;
    private double score;
    private int rank;
    private JsonObject payload = new JsonObject();

    public TournamentEntry(String systemId,String instanceId){
        this();
        this.systemId = systemId;
        this.owner = instanceId;
    }
    public TournamentEntry(String systemId){
        this();
        this.systemId = systemId;

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
    public double score(double delta) {
        score = score+delta;
        if(delta>0){
            this.update();
        }
        return score;
    }
    @Override
    public int rank(){
        return rank;
    }
    public Map<String,Object> toMap(){
        properties.put("1",systemId);
        properties.put("2",score);
        properties.put("3",timestamp);
        properties.put("4",rank);
        properties.put("5",payload.toString());
        return properties;
    }
    public void fromMap(Map<String,Object> properties){
        this.systemId = (String) properties.get("1");
        this.score = ((Number)properties.getOrDefault("2",0)).doubleValue();
        this.timestamp = ((Number)properties.getOrDefault("3",0)).longValue();
        this.rank = ((Number)properties.getOrDefault("4",0)).intValue();
        this.payload = JsonUtil.parse((String)properties.get("5"));
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
    public boolean configureAndValidate(byte[] data){
        payload = JsonUtil.parse(data);
        return true;
    }
    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("systemId",systemId);
        jsonObject.addProperty("score",score);
        jsonObject.addProperty("rank",rank);
        jsonObject.addProperty("timestamp",timestamp);
        jsonObject.add("payload",payload);
        return jsonObject;
    }
}
