package com.tarantula.platform.presence.pvp;

import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class BattleLog extends RecoverableObject {

    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.BATTLE_LOG_CID;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        return true;
    }
}
