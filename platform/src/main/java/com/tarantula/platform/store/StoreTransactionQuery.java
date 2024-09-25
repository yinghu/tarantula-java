package com.tarantula.platform.store;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.SnowflakeKey;

public class StoreTransactionQuery implements RecoverableFactory<StoreTransactionLog> {


    private Recoverable.Key key;

    public StoreTransactionQuery(long systemId){
        this.key = SnowflakeKey.from(systemId);
    }

    @Override
    public StoreTransactionLog create() {
        return new StoreTransactionLog();
    }

    @Override
    public String label() {
        return StoreTransactionLog.LABEL;
    }

    @Override
    public Recoverable.Key key() {
        return key;
    }
}
