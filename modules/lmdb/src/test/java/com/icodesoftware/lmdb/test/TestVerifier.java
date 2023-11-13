package com.icodesoftware.lmdb.test;

public interface TestVerifier {
    void onCommitted(long transactionId);
}
