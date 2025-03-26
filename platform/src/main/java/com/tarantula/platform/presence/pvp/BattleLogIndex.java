package com.tarantula.platform.presence.pvp;

import com.icodesoftware.util.LongCompositeKey;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class BattleLogIndex extends RecoverableObject {

    public long playerId;

    public long offenseTeamId;
    public long defenseTeamId;

    public int offenseEloGain; //positive for win , nagitive for lost
    public int defenseEloGain;
    public int defenseElo; //the elo at end of battle
    public int offenseElo;


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
        buffer.writeInt(offenseElo);
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        offenseTeamId = buffer.readLong();
        defenseTeamId = buffer.readLong();
        offenseEloGain = buffer.readInt();
        defenseEloGain = buffer.readInt();
        defenseElo = buffer.readInt();
        try {
            offenseElo = buffer.readInt();
        }catch (Exception ignored){}
        return true;
    }

    @Override
    public boolean writeKey(DataBuffer buffer) {
        if(playerId==0 || defenseTeamId == 0) return false;
        buffer.writeLong(playerId);
        buffer.writeLong(defenseTeamId);
        return true;
    }

    @Override
    public boolean readKey(DataBuffer buffer) {
        this.playerId = buffer.readLong();
        this.defenseTeamId = buffer.readLong();
        return true;
    }

    @Override
    public long distributionId() {
        return defenseTeamId;
    }

    @Override
    public Key key() {
        return new LongCompositeKey(playerId,defenseTeamId);
    }
}
