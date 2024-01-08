package com.perfectday.games.earth8;

import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.icodesoftware.service.ApplicationPreSetup;
import com.icodesoftware.service.TokenValidatorProvider;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.RecoverableObject;
import com.perfectday.games.earth8.analytics.AnalyticsTransaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BattleUpdate extends RecoverableObject {

    public enum UpdateId {
        UnitXpUP,
        UnitRankUp,
        UnitSkillUp,
        EquipmentXpUp,
        EquipmentRankUp,
        EquipmentSalvage,
        EquipmentEquip,
        EquipmentUnEquip,
        CampaignProgress,
    }

    public UpdateId updateId;
    public long unitId;
    public long equipmentId;

    public HashMap<String,Integer> currencies = new HashMap<>();

    protected List<AnalyticsTransaction> pendingAnalytics = new ArrayList<>();

    //Data store write contract
    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeInt(updateId.ordinal());
        buffer.writeLong(unitId);
        buffer.writeLong(equipmentId);
        buffer.writeInt(currencies.size());
        currencies.forEach((k,v)->{
            buffer.writeUTF8(k);
            buffer.writeInt(v);
        });
        return true;
    }

    //Data store read contract
    @Override
    public boolean read(DataBuffer buffer) {
        updateId = UpdateId.values()[buffer.readInt()];
        unitId = buffer.readLong();
        equipmentId = buffer.readLong();
        int csize = buffer.readInt();
        for(int i =0;i<csize;i++){
            currencies.put(buffer.readUTF8(),buffer.readInt());
        }
        return true;
    }

    //LIMIT RESPONSE SIZE FROM DEFAULT JSON-CONVERT
    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Successful",true);
        jsonObject.addProperty("BattleId",distributionId);
        return jsonObject;
    }

    public static BattleUpdate fromJson(byte[] json){
        JsonObject jsonObject = JsonUtil.parse(json);
        UpdateId target = UpdateId.values()[jsonObject.get("UpdateId").getAsInt()];
        BattleUpdate update = null;
        switch (target) {
            case UnitXpUP:
                update = UnitXpUp.fromJson(jsonObject);
                break;
            case UnitRankUp:
                update = UnitRankUp.fromJson(jsonObject);
                break;
            case UnitSkillUp:
                update = UnitSkillUp.fromJson(jsonObject);
                break;
            case EquipmentXpUp:
                update = EquipmentXpUp.fromJson(jsonObject);
                break;
            case EquipmentRankUp:
                update = EquipmentRankUp.fromJson(jsonObject);
                break;
            case EquipmentSalvage:
                update = EquipmentSalvage.fromJson(jsonObject);
                break;
            case EquipmentEquip:
                update = EquipmentEquip.fromJson(jsonObject);
                break;
            case EquipmentUnEquip:
                update = EquipmentUnEquip.fromJson(jsonObject);
                break;
            case CampaignProgress:
                update = CampaignProgress.fromJson(jsonObject);
                break;
            default:
                throw new UnsupportedOperationException("operation not supported");
        }
        return update;
    }

    protected void parse(JsonObject json){
        updateId = UpdateId.values()[GetJsonInt(json, "UpdateId", 0)];
        unitId = GetJsonLong(json, "UnitId", 0);
        equipmentId = GetJsonLong(json, "EquipmentId", 0);

        if(!json.has("Currencies")) return;
        JsonObject cmap = json.get("Currencies").getAsJsonObject();
        cmap.entrySet().forEach(entry->{
            currencies.put(entry.getKey(),entry.getValue().getAsInt());
        });
    }
    protected boolean runUpdate(ApplicationPreSetup applicationPreSetup, Session session){
        return true;
    }

    public boolean update(ApplicationPreSetup applicationPreSetup, Session session){
        return runUpdate(applicationPreSetup, session);
    }

    public void publishAnalytics(TokenValidatorProvider.AuthVendor webhook,String query){
        pendingAnalytics.forEach(analyticsTransaction -> {
            webhook.upload(query,analyticsTransaction.toString().getBytes());
        });
    }

    @Override
    public int getFactoryId() {
        return Earth8PortableRegistry.OID;
    }

    protected static int GetJsonInt(JsonObject obj, String key, int defaultVal)
    {
        if(obj.has(key)) return obj.get(key).getAsInt();
        return defaultVal;
    }

    protected static long GetJsonLong(JsonObject obj, String key, long defaultVal)
    {
        if(obj.has(key)) return obj.get(key).getAsLong();
        return defaultVal;
    }

    protected static String GetJsonString(JsonObject obj, String key, String defaultVal)
    {
        if(obj.has(key)) return obj.get(key).getAsString();
        return defaultVal;
    }

}
