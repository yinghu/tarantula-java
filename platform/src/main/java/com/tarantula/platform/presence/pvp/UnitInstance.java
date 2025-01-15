package com.tarantula.platform.presence.pvp;

import com.google.gson.JsonObject;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class UnitInstance extends RecoverableObject {

    public static final int MAX_ABILITY_RANKS = 4;

    public int level;
    public int rank;
    public int currentLevelExperience;
    public int currentRankExperience;
    public String configID;

    public long weaponIDValue;
    public long helmetIDValue;
    public long chestPieceIDValue;
    public long glovesIDValue;
    public long forceFieldIDValue;
    public long bootsIDValue;

    public final int[] abilityRanks = new int[MAX_ABILITY_RANKS]; // 0-4
    public int passiveRank;

    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.UNIT_INSTANCE_CID;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        this.level = buffer.readInt();
        this.rank = buffer.readInt();
        this.currentLevelExperience = buffer.readInt();
        this.currentRankExperience = buffer.readInt();
        this.configID = buffer.readUTF8();
        this.weaponIDValue = buffer.readLong();
        this.helmetIDValue = buffer.readLong();
        this.chestPieceIDValue = buffer.readLong();
        this.glovesIDValue = buffer.readLong();
        this.forceFieldIDValue = buffer.readLong();
        this.bootsIDValue = buffer.readLong();
        for(int i=0;i<MAX_ABILITY_RANKS;i++){
            abilityRanks[i] = buffer.readInt();
        }
        passiveRank = buffer.readInt();
        return true;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeInt(level);
        buffer.writeInt(rank);
        buffer.writeInt(currentLevelExperience);
        buffer.writeInt(currentRankExperience);
        buffer.writeUTF8(configID);
        buffer.writeLong(weaponIDValue);
        buffer.writeLong(helmetIDValue);
        buffer.writeLong(chestPieceIDValue);
        buffer.writeLong(glovesIDValue);
        buffer.writeLong(forceFieldIDValue);
        buffer.writeLong(bootsIDValue);
        for(int i=0;i<MAX_ABILITY_RANKS;i++){
            buffer.writeInt(abilityRanks[0]);
        }
        buffer.writeInt(passiveRank);
        return true;
    }

    @Override
    public JsonObject toJson() {
        JsonObject resp = new JsonObject();
        resp.add("levelAndRank", DefenseTeam.levelAndRank(level,rank,currentLevelExperience,currentRankExperience));
        resp.addProperty("configID",configID);
        resp.add("equipment", DefenseTeam.equipment(weaponIDValue,helmetIDValue,chestPieceIDValue,glovesIDValue,forceFieldIDValue,bootsIDValue));
        resp.add("abilityRanks", DefenseTeam.abilityRanks(abilityRanks,passiveRank));
        return resp;
    }
}
