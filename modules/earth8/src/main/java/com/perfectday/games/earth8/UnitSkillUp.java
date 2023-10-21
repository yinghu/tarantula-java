package com.perfectday.games.earth8;

import com.google.gson.JsonObject;

public class UnitSkillUp extends BattleUpdate{

    public long skillId;
    public int level;
    @Override
    public boolean write(DataBuffer buffer) {
        if(!super.write(buffer)) return false;
        buffer.writeLong(skillId);
        buffer.writeInt(level);
        return true;
    }

    //Data store read contract
    @Override
    public boolean read(DataBuffer buffer) {
        super.read(buffer);
        skillId = buffer.readLong();
        level = buffer.readInt();
        return true;
    }
    @Override
    public int getClassId() {
        return Earth8PortableRegistry.UNIT_SKILL_UP_CID;
    }


    public static UnitSkillUp fromJson(JsonObject jsonObject){
        UnitSkillUp unitRankUp = new UnitSkillUp();
        unitRankUp.parse(jsonObject);
        unitRankUp.skillId = jsonObject.get("SkillId").getAsLong();
        unitRankUp.level = jsonObject.get("Level").getAsInt();
        return unitRankUp;
    }

}
