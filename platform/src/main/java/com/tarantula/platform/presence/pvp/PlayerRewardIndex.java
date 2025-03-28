package com.tarantula.platform.presence.pvp;

import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class PlayerRewardIndex extends RecoverableObject {
    public static final String POST_BATTLE = "reward_post_battle";
    public static final String PLACEMENT = "reward_placement";
    public static final String LEAGUE = "reward_league";

    public long rewardId;
    public PlayerRewardIndex(){

    }

    public PlayerRewardIndex(long playerId,String label) {
        this();
        this.distributionId = playerId;
        this.label = label;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeLong(rewardId);
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        rewardId = buffer.readLong();

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
