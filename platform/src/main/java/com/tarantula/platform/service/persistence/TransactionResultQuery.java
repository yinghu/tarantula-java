package com.tarantula.platform.service.persistence;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.SnowflakeKey;

public class TransactionResultQuery implements RecoverableFactory<TransactionResult> {

    public long nodeId;
    public TransactionResultQuery(long nodeId){
        this.nodeId = nodeId;
    }

    @Override
    public TransactionResult create() {
        return new TransactionResult();
    }

    @Override
    public String label() {
        return TransactionResult.LABEL;
    }
    @Override
    public Recoverable.Key key() {
        return new SnowflakeKey(nodeId);
    }
}
