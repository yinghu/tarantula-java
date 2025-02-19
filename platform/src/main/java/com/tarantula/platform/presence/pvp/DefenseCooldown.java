package com.tarantula.platform.presence.pvp;

import com.icodesoftware.util.RecoverableObject;

public class DefenseCooldown extends RecoverableObject {

    public long cooldownTimer;

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
