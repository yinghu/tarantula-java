package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;
import com.tarantula.platform.item.ConfigurableObject;

import java.util.Map;

public class TournamentPrize extends ConfigurableObject implements Tournament.Prize {

    public int rank;

    public TournamentPrize(){
        this.onEdge = true;
        this.label = Tournament.PRIZE_LABEL;
    }
    @Override
    public Map<String,Object> toMap(){
        super.toMap();
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        super.fromMap(properties);
    }

    public int getFactoryId() {
        return TournamentPortableRegistry.OID;
    }
    public int getClassId() {
        return TournamentPortableRegistry.TOURNAMENT_PRIZE_CID;
    }

    @Override
    public int rank() {
        return rank;
    }

    @Override
    public boolean configureAndValidate(){
        boolean valid = this.header.has("name")&&this.header.has("rank");
        name = header.get("name").getAsString();
        rank = header.get("rank").getAsInt();
        return valid;
    }
    public String toString(){
        return "Tournament Prize->"+name+"<>"+rank;
    }
}
