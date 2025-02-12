package com.tarantula.platform.presence.pvp;

import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class BattleLogIndex extends RecoverableObject {

    public long offenseTeamId;
    public long defenseTeamId;

    public int offenseEloGain; //positive for win , nagitive for lost
    public int defenseEloGain;
    public int defenseElo; //the elo at end of battle

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
        buffer.writeLong(offenseTeamId);
        buffer.writeLong(defenseTeamId);
        buffer.writeInt(offenseEloGain);
        buffer.writeInt(defenseEloGain);
        buffer.writeInt(defenseElo);
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        offenseTeamId = buffer.readLong();
        defenseTeamId = buffer.readLong();
        offenseEloGain = buffer.readInt();
        defenseEloGain = buffer.readInt();
        defenseElo = buffer.readInt();
        return true;
    }

}
