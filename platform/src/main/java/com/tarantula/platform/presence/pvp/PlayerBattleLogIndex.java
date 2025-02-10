package com.tarantula.platform.presence.pvp;

import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class PlayerBattleLogIndex extends RecoverableObject {

    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.PLAYER_BATTLE_LOG_INDEX_CID;
    }

}
