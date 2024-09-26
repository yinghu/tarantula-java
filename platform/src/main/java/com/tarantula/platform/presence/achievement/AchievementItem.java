package com.tarantula.platform.presence.achievement;

import com.google.gson.JsonObject;
import com.tarantula.platform.item.*;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.util.List;

public class AchievementItem extends Application{

    public AchievementItem(){}

    public AchievementItem(JsonObject payload){
        super(payload);
    }

    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.ACHIEVEMENT_CID;
    }

    public int tier(){
        return header.get("Tier").getAsInt();
    }
    public int target(){
        return header.get("Target").getAsInt();
    }
    @Override
    public String name(){
        return "tier_"+header.get("Tier").getAsInt()+"_target_"+header.get("Target").getAsInt();
    }
    public double objective(){
        return header.get("Objective").getAsDouble();
    }

    @Override
    public boolean configureAndValidate() {
        setup();
        return validated;
    }

    @Override
    public JsonObject toJson() {
        if(header.has("_award")){
            return header;
        }
        return super.toJson();
    }

    @Override
    public List<Commodity> commodityList(){
        List<Commodity> commodities = super.commodityList();
        header.get("_award").getAsJsonObject().get("_skuList").getAsJsonArray().forEach(e->{
            commodities.add(new Commodity(e.getAsJsonObject().get("_sku").getAsJsonObject()));
        });
        return commodities;
    }
}
