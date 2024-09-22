package com.tarantula.platform.presence.dailygiveaway;

import com.google.gson.JsonObject;
import com.tarantula.platform.item.*;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.util.List;

public class DailyGiveaway extends Application {

    public DailyGiveaway(){

    }

    public DailyGiveaway(JsonObject payload){
        super(payload);
    }

    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.DAILY_GIVEAWAY_CID;
    }

    public int tier(){
        return header.get("Tier").getAsInt();
    }
    public int day(){
        return header.get("Day").getAsInt();
    }

    @Override
    public String name(){
        return "t_"+tier()+"_d_"+day();
    }


    @Override
    public JsonObject toJson() {
        if(header.has("_reward")){
            return header;
        }
        return super.toJson();
    }

    @Override
    public boolean configureAndValidate() {
        setup();
        return validated;
    }

    @Override
    public List<Commodity> commodityList(){
        List<Commodity> commodities = super.commodityList();
        header.get("_reward").getAsJsonObject().get("_skuList").getAsJsonArray().forEach(e->{
            commodities.add(new Commodity(e.getAsJsonObject().get("_sku").getAsJsonObject()));
        });
        return commodities;
    }

}
