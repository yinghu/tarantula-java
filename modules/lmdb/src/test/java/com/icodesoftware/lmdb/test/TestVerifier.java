package com.icodesoftware.lmdb.test;

public interface TestVerifier {
    void onTransaction(int scope,long transactionId);
}
