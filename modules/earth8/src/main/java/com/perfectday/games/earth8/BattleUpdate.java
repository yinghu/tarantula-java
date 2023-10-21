package com.perfectday.games.earth8;

import com.google.gson.JsonObject;
import com.icodesoftware.service.ApplicationPreSetup;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.RecoverableObject;

import java.util.HashMap;

public class BattleUpdate extends RecoverableObject {

    public enum UpdateId {UnitXpUP,UnitRankUp,UnitSkillUp,EquipmentXpUp,EquipmentRankUp,EquipmentSalvage,EquipmentEquip,EquipmentUnEquip}

    public UpdateId updateId;
    public long unitId;
    public long equipmentId;

    public HashMap<String,Integer> currencies = new HashMap<>();

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
            default:
                throw new UnsupportedOperationException("operation not supported");
        }
        return update;
    }

    protected void parse(JsonObject json){
        updateId = UpdateId.values()[json.get("UpdateId").getAsInt()];
        unitId = json.get("UnitId").getAsLong();
        equipmentId = json.get("EquipmentId").getAsLong();
        if(!json.has("Currencies")) return;
        JsonObject cmap = json.get("Currencies").getAsJsonObject();
        cmap.entrySet().forEach(entry->{
            currencies.put(entry.getKey(),entry.getValue().getAsInt());
        });
    }
    protected boolean runUpdate(ApplicationPreSetup applicationPreSetup){
        return true;
    }

    public boolean update(ApplicationPreSetup applicationPreSetup){
        return runUpdate(applicationPreSetup);
    }

    @Override
    public int getFactoryId() {
        return Earth8PortableRegistry.OID;
    }

}
