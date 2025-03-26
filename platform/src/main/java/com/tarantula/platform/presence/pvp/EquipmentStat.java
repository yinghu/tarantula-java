package com.tarantula.platform.presence.pvp;

import com.google.gson.JsonObject;
import com.icodesoftware.util.RecoverableObject;

public class EquipmentStat extends RecoverableObject {

    public float subStatRolledPercentageNormalized;
    public boolean hasSubStatRoll;
    public String statConfigID;

    @Override
    public JsonObject toJson() {
        JsonObject resp = new JsonObject();
        resp.addProperty("subStatRolledPercentageNormalized",subStatRolledPercentageNormalized);
        resp.addProperty("hasSubStatRoll",hasSubStatRoll);
        resp.addProperty("statConfigID",statConfigID);
        return resp;
    }

}
