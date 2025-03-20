package com.tarantula.platform.presence.pvp;

import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class BotIndex extends RecoverableObject {

    public static final String LABEL = "defense_bot_index";
    public long teamId;

    public BotIndex(long teamId) {
        this();
        this.teamId = teamId;
    }

    public BotIndex() {
        this.onEdge = true;
        this.label = LABEL;
    }

    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.BOT_INDEX_CID;
    }

    @Override
    public boolean write(DataBuffer buffer) {//tell database what needs to save
        buffer.writeLong(teamId);
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {//fetch data from database
        teamId = buffer.readLong();
        return true;
    }
}
