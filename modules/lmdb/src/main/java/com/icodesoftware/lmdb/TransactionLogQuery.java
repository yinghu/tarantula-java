package com.icodesoftware.lmdb;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.SnowflakeKey;

public class TransactionLogQuery implements RecoverableFactory<TransactionLog> {

    public long transactionId;
    public TransactionLogQuery(long transactionId){
        this.transactionId = transactionId;
    }

    @Override
    public TransactionLog create() {
        return new TransactionLog();
    }

    @Override
    public String label() {
        return TransactionLog.LABEL;
    }
    @Override
    public Recoverable.Key key() {
        return new SnowflakeKey(transactionId);
    }
}
