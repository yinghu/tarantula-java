package com.perfectday.games.earth8.inbox;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.SnowflakeKey;

public class ItemGrantEventQuery implements RecoverableFactory<ItemGrantEvent> {
    private long systemId;
    public ItemGrantEventQuery(long systemId){
        this.systemId = systemId;
    }
    @Override
    public ItemGrantEvent create() {
        return new ItemGrantEvent();
    }

    @Override
    public String label() {
        return ItemGrantEvent.LABEL;
    }

    @Override
    public Recoverable.Key key() {
        return SnowflakeKey.from(systemId);
    }
}
