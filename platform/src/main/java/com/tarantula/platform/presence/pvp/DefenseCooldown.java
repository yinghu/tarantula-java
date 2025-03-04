package com.tarantula.platform.presence.pvp;

import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class DefenseCooldown extends RecoverableObject {

    public static final String LABEL = "cool_down";

    public DefenseCooldown(){
        this.label = LABEL;
    }

    public DefenseCooldown(long teamId){
        this();
        this.distributionId = teamId;
    }

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
        timestamp = buffer.readLong();
        return true;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeLong(timestamp);
        return true;
    }

    @Override
    public boolean writeKey(DataBuffer buffer) {
        buffer.writeLong(distributionId);
        buffer.writeUTF8(label);
        return true;
    }

    @Override
    public boolean readKey(DataBuffer buffer) {
        this.distributionId = buffer.readLong();
        this.label = buffer.readUTF8();
        return true;
    }

    @Override
    public Key key(){
        return new AssociateKey(distributionId,label);
    }
}
