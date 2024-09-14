package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;
import com.tarantula.platform.item.Application;
import com.tarantula.platform.item.ConfigurableObject;


public class TournamentPrize extends Application implements Tournament.Prize {

    private int rank;

    public TournamentPrize(){

    }

    public TournamentPrize(ConfigurableObject configurableObject,int rank){
        super(configurableObject);
        this.rank = rank;
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
        boolean valid = this.header.has("MinRank") && this.header.has("MaxRank");
        //rank = header.get("MinRank").getAsInt();
        return valid;
    }
    public String toString(){
        return "Tournament Prize Rank>>>"+rank;
    }


}
