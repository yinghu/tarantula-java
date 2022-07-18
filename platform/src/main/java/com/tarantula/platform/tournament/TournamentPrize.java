package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;
import com.tarantula.platform.item.Application;
import com.tarantula.platform.item.ConfigurableObject;

import java.util.Map;

public class TournamentPrize extends Application implements Tournament.Prize {

    private int rank;

    public TournamentPrize(){

    }

    public TournamentPrize(ConfigurableObject configurableObject){
        super(configurableObject);
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
        boolean valid = this.header.has("Rank");
        rank = header.get("Rank").getAsInt();
        return valid;
    }
    public String toString(){
        return "Tournament Prize Rank>>>"+rank;
    }

}
