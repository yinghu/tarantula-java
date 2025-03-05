package com.tarantula.platform.presence.pvp;

import com.google.gson.JsonObject;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.item.Application;

public class RewardList extends RecoverableObject {
    public Application placementReward;
    public Application leagueReward;

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        if(placementReward!=null) jsonObject.add("_placementReward",placementReward.toJson());
        if(leagueReward!=null) jsonObject.add("_leagueReward",leagueReward.toJson());
        return jsonObject;
    }
}
