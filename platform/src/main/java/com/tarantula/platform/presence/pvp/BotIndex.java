package com.tarantula.platform.presence.pvp;

import com.icodesoftware.util.RecoverableObject;

public class BotIndex extends RecoverableObject {
    public long teamId;

    public BotIndex(long teamId) {
        this.teamId = teamId;
    }

    public BotIndex() {
    }
}
