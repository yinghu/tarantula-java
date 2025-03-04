package com.tarantula.platform.presence.pvp;

import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class PlayerRewardIndex extends RecoverableObject {
    public static final String LABEL = "reward_index";

    public long postBattleRewardId;
    public long placementRewardId;
    public long leagueRewardId;

    public PlayerRewardIndex(){
        this.label = LABEL;
    }

    public PlayerRewardIndex(long playerId) {
        this();
        this.distributionId = playerId;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeLong(postBattleRewardId);
        buffer.writeLong(placementRewardId);
        buffer.writeLong(leagueRewardId);
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        postBattleRewardId = buffer.readLong();
        placementRewardId  = buffer.readLong();
        leagueRewardId = buffer.readLong();
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
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.PLAYER_REWARD_INDEX_CID;
    }

    @Override
    public Key key(){
        return new AssociateKey(distributionId,label);
    }
}
