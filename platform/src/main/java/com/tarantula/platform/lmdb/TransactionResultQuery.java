package com.tarantula.platform.lmdb;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.Transaction;
import com.icodesoftware.util.SnowflakeKey;
public class TransactionResultQuery implements RecoverableFactory<Transaction.History> {

    public long nodeId;
    public TransactionResultQuery(long nodeId){
        this.nodeId = nodeId;
    }

    @Override
    public Transaction.History create() {
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
