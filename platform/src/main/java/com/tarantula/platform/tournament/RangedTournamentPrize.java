package com.tarantula.platform.tournament;

import com.google.gson.JsonObject;
import com.tarantula.platform.item.ConfigurableEdit;

import java.util.ArrayList;
import java.util.List;

public class RangedTournamentPrize extends ConfigurableEdit {

    private int minRank;
    private int maxRank;

    public RangedTournamentPrize(JsonObject payload){
        this.header = payload;
        this.minRank = payload.get("MinRank").getAsInt();
        this.maxRank = payload.get("MaxRank").getAsInt();
    }

    @Override
    public JsonObject toJson(){
        return this.header;
    }

    public List<TournamentPrize> prizeList(){
        ArrayList<TournamentPrize> prizes = new ArrayList<>();
        for(int rank = minRank;rank<=maxRank;rank++){
            prizes.add(new TournamentPrize(header,rank));
        }
        return prizes;
    }
}
