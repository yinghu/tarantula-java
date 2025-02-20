package com.tarantula.platform.presence.pvp;

import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class DefenseCooldown extends RecoverableObject {

    public long cooldownTimer;

    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.DEFENSE_COOLDOWN_CID;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        cooldownTimer = buffer.readLong();
        return true;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeLong(cooldownTimer);
        return true;
    }
}
