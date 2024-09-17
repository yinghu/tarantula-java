package com.tarantula.platform.tournament;

import com.google.gson.JsonObject;
import com.icodesoftware.Tournament;
import com.tarantula.platform.item.Application;
import com.tarantula.platform.item.Commodity;
import com.tarantula.platform.item.ConfigurableObject;

import java.util.ArrayList;
import java.util.List;


public class TournamentPrize extends Application implements Tournament.Prize {

    private int rank;

    public TournamentPrize(){

    }

    public TournamentPrize(JsonObject payload,int rank){
        this.header = payload;
        this.rank = rank;
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
        return valid;
    }
    public String toString(){
        return "Tournament Prize Rank>>>"+rank;
    }

    public List<Commodity> commodityList(){
        if(!header.has("_skuList")) return null;
        ArrayList<Commodity> commodities = new ArrayList<>();
        header.get("_skuList").getAsJsonArray().forEach(e->{
            JsonObject sku = e.getAsJsonObject().get("_sku").getAsJsonObject();
            commodities.add(new Commodity(sku));
        });
        return commodities;
    }


}
