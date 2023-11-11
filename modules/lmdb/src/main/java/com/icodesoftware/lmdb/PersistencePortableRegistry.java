package com.icodesoftware.lmdb;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.AbstractRecoverableListener;

public class PersistencePortableRegistry<T extends Recoverable> extends AbstractRecoverableListener {

    public static final int OID = 8;

    public static final int TRANSACTION_LOG_CID = 1;
    public static final int TRANSACTION_RESULT_CID = 2;
    @Override
    public int registryId() {
        return OID;
    }

    @Override
    public Recoverable create(int cid) {
        Recoverable _ins;
        switch(cid){
            case TRANSACTION_LOG_CID:
                _ins = new TransactionLog();
                break;
            case TRANSACTION_RESULT_CID:
                _ins = new TransactionResult();
                break;
            default:
                throw new RuntimeException("Class ID ["+cid+"] not supported");
        }
        return _ins;
    }
}
