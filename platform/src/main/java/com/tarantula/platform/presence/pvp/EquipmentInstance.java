package com.tarantula.platform.presence.pvp;

import com.google.gson.JsonObject;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class EquipmentInstance extends RecoverableObject {

    public static final int MAX_EQUIPMENT_STATS = 5;

    public int level;
    public int rank;
    public int currentLevelExperience;
    public int currentRankExperience;

    public final EquipmentStat primaryStat = new EquipmentStat();
    public final EquipmentStat[] subStats = new EquipmentStat[MAX_EQUIPMENT_STATS];//0- 5

    public String configID;
    public String setConfigID;
    public String rewardConfigID;
    public long snowflakeIDValue;

    public EquipmentInstance() {
        for(int i=0;i<MAX_EQUIPMENT_STATS;i++){
            subStats[i]=new EquipmentStat();
        }
    }
    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.EQUIPMENT_INSTANCE_CID;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        this.level = buffer.readInt();
        this.rank = buffer.readInt();
        this.currentLevelExperience = buffer.readInt();
        this.currentRankExperience = buffer.readInt();
        this.primaryStat.subStatRolledPercentageNormalized = buffer.readFloat();
        this.primaryStat.hasSubStatRoll = buffer.readBoolean();
        this.primaryStat.statConfigID = buffer.readUTF8();
        for(int i=0;i<MAX_EQUIPMENT_STATS;i++){
            subStats[i].subStatRolledPercentageNormalized = buffer.readFloat();
            subStats[i].hasSubStatRoll = buffer.readBoolean();
            subStats[i].statConfigID = buffer.readUTF8();
        }
        this.configID = buffer.readUTF8();
        this.setConfigID = buffer.readUTF8();
        this.rewardConfigID = buffer.readUTF8();
        this.snowflakeIDValue = buffer.readLong();
        return true;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeInt(level);
        buffer.writeInt(rank);
        buffer.writeInt(currentLevelExperience);
        buffer.writeInt(currentRankExperience);
        buffer.writeFloat(primaryStat.subStatRolledPercentageNormalized);
        buffer.writeBoolean(primaryStat.hasSubStatRoll);
        buffer.writeUTF8(primaryStat.statConfigID); //60
        for(int i=0;i<MAX_EQUIPMENT_STATS;i++){ //300
            buffer.writeFloat(subStats[i].subStatRolledPercentageNormalized);
            buffer.writeBoolean(subStats[i].hasSubStatRoll);
            buffer.writeUTF8(subStats[i].statConfigID);
        }
        buffer.writeUTF8(configID);
        buffer.writeUTF8(setConfigID);
        buffer.writeUTF8(rewardConfigID); //150
        buffer.writeLong(snowflakeIDValue);
        return true;
    }

    @Override
    public JsonObject toJson() {
        JsonObject resp = new JsonObject();
        resp.add("levelAndRank",OffenseTeam.levelAndRank(level,rank,currentLevelExperience,currentRankExperience));
        resp.add("primaryStat",primaryStat.toJson());
        resp.add("subStats",OffenseTeam.subStats(subStats));
        resp.addProperty("configID",configID);
        resp.addProperty("setConfigID",setConfigID);
        resp.addProperty("rewardConfigID",rewardConfigID);
        resp.addProperty("snowflakeIDValue",snowflakeIDValue);
        return resp;
    }
}
