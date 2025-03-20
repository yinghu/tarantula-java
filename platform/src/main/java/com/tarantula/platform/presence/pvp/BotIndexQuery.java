package com.tarantula.platform.presence.pvp;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.SnowflakeKey;

public class BotIndexQuery implements RecoverableFactory<BotIndex> {

    private long nodeId;

    public BotIndexQuery(long nodeId){
        this.nodeId = nodeId;
    }
    @Override
    public BotIndex create() {
        return new BotIndex();
    }

    @Override
    public String label() {
        return BotIndex.LABEL;
    }

    @Override
    public Recoverable.Key key() {
        return SnowflakeKey.from(nodeId);
    }
}
