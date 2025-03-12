package com.tarantula.platform.presence.pvp;

import com.icodesoftware.util.IntegerKey;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class DefenseTeamIndex extends RecoverableObject {

    public static final String POOL_LABEL = "team_pool_index";
    public static final String PLAYER_LABEL = "team_player_index";


    public long playerId;

    public DefenseTeamIndex(){
        this.label = POOL_LABEL;
        this.onEdge = true;
    }

    public DefenseTeamIndex(IntegerKey poolKey){
        this();
        this.ownerKey = poolKey;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        this.playerId = buffer.readLong();
        this.timestamp = buffer.readLong();
        return true;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        if(playerId==0) return false;
        buffer.writeLong(playerId);
        buffer.writeLong(timestamp);
        return true;
    }

    @Override
    public int getClassId() {
        return PresencePortableRegistry.BATTLE_TEAM_INDEX_CID;
    }

    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }

    public long teamId(){
        return distributionId;
    }

    public boolean onCooldown(){
        if(timestamp==0) return false;
        return !TimeUtil.expired(TimeUtil.fromUTCMilliseconds(timestamp));
    }
}
