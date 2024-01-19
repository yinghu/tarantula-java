package com.icodesoftware.protocol;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.AbstractRecoverableListener;

public class ProtocolPortableRegistry<T extends Recoverable> extends AbstractRecoverableListener {

    public static final int OID = 9;

    //public static final int TRANSACTION_LOG_CID = 1;
    public static final int STATISTICS_CID = 3;
    public static final int STATISTICS_ENTRY_CID = 5;
    @Override
    public int registryId() {
        return OID;
    }

    @Override
    public T create(int cid) {
        Recoverable _ins;
        switch(cid){
            //case TRANSACTION_LOG_CID:
                //_ins = new TransactionLog();
                //break;
            //case TRANSACTION_RESULT_CID:
                //_ins = new TransactionResult();
                //break;
            default:
                throw new RuntimeException("Class ID ["+cid+"] not supported");
        }
        //return (T)_ins;
    }
}
