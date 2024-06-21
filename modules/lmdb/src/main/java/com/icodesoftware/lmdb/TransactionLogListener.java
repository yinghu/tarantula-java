package com.icodesoftware.lmdb;

public interface TransactionLogListener {

    void onTransactionLog(TransactionLog transactionLog);
}
