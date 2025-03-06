package com.tarantula.platform.presence.pvp;

import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class PlayerBattleLogIndex extends RecoverableObject {

    public long battleId0;
    public long battleId1;
    public long battleId2;
    public long battleId3;
    public long battleId4;
    public long battleId5;
    public long battleId6;
    public long battleId7;
    public long battleId8;
    public long battleId9;

    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.PLAYER_BATTLE_LOG_INDEX_CID;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        battleId0 = buffer.readLong();
        battleId1 = buffer.readLong();
        battleId2 = buffer.readLong();
        battleId3 = buffer.readLong();
        battleId4 = buffer.readLong();
        try {
            battleId5 = buffer.readLong();
            battleId6 = buffer.readLong();
            battleId7 = buffer.readLong();
            battleId8 = buffer.readLong();
            battleId9 = buffer.readLong();
        }catch (Exception ignored){}
        return true;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeLong(battleId0);
        buffer.writeLong(battleId1);
        buffer.writeLong(battleId2);
        buffer.writeLong(battleId3);
        buffer.writeLong(battleId4);
        buffer.writeLong(battleId5);
        buffer.writeLong(battleId6);
        buffer.writeLong(battleId7);
        buffer.writeLong(battleId8);
        buffer.writeLong(battleId9);
        return true;
    }

    public void updateOffenseLogs(long latestBattleId){
        battleId0 = battleId1;
        battleId1 = battleId2;
        battleId2 = battleId3;
        battleId3 = battleId4;
        battleId4 = latestBattleId;
        update();
    }

    public void updateDefenseLogs(long latestBattleId){
        battleId5 = battleId6;
        battleId6 = battleId7;
        battleId7 = battleId8;
        battleId8 = battleId9;
        battleId9 = latestBattleId;
        update();
    }

    public long[] getOffenseBattles(){
        return new long[]{battleId0, battleId1, battleId2, battleId3, battleId4};
    }

    public long[] getDefenseBattles(){
        return new long[]{battleId5, battleId6, battleId7, battleId8, battleId9};
    }

}
