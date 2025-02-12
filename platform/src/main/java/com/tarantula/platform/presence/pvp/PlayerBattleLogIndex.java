package com.tarantula.platform.presence.pvp;

import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class PlayerBattleLogIndex extends RecoverableObject {

    public long battleId0;
    public long battleId1;
    public long battleId2;
    public long battleId3;
    public long battleId4;

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
        return true;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeLong(battleId0);
        buffer.writeLong(battleId1);
        buffer.writeLong(battleId2);
        buffer.writeLong(battleId3);
        buffer.writeLong(battleId4);
        return true;
    }
}
