package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.util.Map;

public class TournamentEntry extends RecoverableObject implements Tournament.Entry {

    private String systemId;
    private double score;
    private int rank;
    private Tournament.Listener listener;

    public TournamentEntry(String systemId, Tournament.Listener listener){
        this();
        this.systemId = systemId;
        this.listener = listener;
    }
    public TournamentEntry(Tournament.Listener listener){
        this();
        this.listener = listener;
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
            listener.onUpdated(this);
        }
        return score;
    }
    @Override
    public int rank(){
        return rank;
    }
    public Map<String,Object> toMap(){
        properties.put("1",systemId);
        properties.put("4",score);
        properties.put("5",timestamp);
        properties.put("6",rank);
        return properties;
    }
    public void fromMap(Map<String,Object> properties){
        this.systemId = (String) properties.get("1");
        this.score = ((Number)properties.getOrDefault("4",0)).doubleValue();
        this.timestamp = ((Number)properties.getOrDefault("5",0)).longValue();
        this.rank = ((Number)properties.getOrDefault("6",0)).intValue();
    }
    @Override
    public int getFactoryId() {
        return TournamentPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return TournamentPortableRegistry.TOURNAMENT_ENTRY_CID;
    }
}
