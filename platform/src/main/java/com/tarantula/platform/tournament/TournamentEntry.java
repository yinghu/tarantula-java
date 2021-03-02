package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.util.Map;

public class TournamentEntry extends RecoverableObject implements Tournament.Entry {

    private String systemId;
    private String name;
    private String icon;
    private double score;
    private Tournament.Listener listener;

    public TournamentEntry(String systemId, Tournament.Listener listener){
        this();
        this.systemId = systemId;
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
    public String name() {
        return name;
    }

    @Override
    public void name(String name) {
        this.name = name;
    }

    @Override
    public String icon() {
        return icon;
    }

    @Override
    public void icon(String icon) {
        this.icon = icon;
    }

    @Override
    public double score(double delta) {
        score = score+delta;
        if(delta>0){
            listener.onUpdate(this);
        }
        return score;
    }
    public Map<String,Object> toMap(){
        properties.put("1",systemId);
        properties.put("2",name);
        properties.put("3",icon);
        properties.put("4",score);
        properties.put("5",timestamp);
        return properties;
    }
    public void fromMap(Map<String,Object> properties){
        this.systemId = (String) properties.get("1");
        this.name = (String) properties.getOrDefault("2","name");
        this.icon = (String) properties.getOrDefault("3","icon");
        this.score = ((Number)properties.getOrDefault("4",0)).doubleValue();
        this.timestamp = ((Number)properties.getOrDefault("5",0)).longValue();
    }
    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PresencePortableRegistry.TOURNAMENT_ENTRY_CID;
    }
}
