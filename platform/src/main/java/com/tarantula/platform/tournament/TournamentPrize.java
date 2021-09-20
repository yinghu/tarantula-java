package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;
import com.icodesoftware.util.RecoverableObject;

import java.util.Map;

public class TournamentPrize extends RecoverableObject implements Tournament.Prize {

    public int rank;
    public String itemId;

    public TournamentPrize(){
        this.onEdge = true;
        this.label = Tournament.PRIZE_LABEL;
    }
    @Override
    public Map<String,Object> toMap(){
        properties.put("1",rank);
        properties.put("2",itemId);
        return properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        rank = ((Number)properties.get("1")).intValue();
        itemId = (String) properties.get("2");
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
}
