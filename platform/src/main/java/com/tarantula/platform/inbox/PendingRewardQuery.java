package com.tarantula.platform.inbox;


import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.SnowflakeKey;

public class PendingRewardQuery implements RecoverableFactory<PendingReward> {


    private Recoverable.Key key;

    public PendingRewardQuery(long systemId) {
        this.key = SnowflakeKey.from(systemId);

    }
    @Override
    public PendingReward create() {
        return new PendingReward();
    }

    @Override
    public String label() {
        return PendingReward.LABEL;
    }

    @Override
    public Recoverable.Key key() {
        return key;
    }

}
