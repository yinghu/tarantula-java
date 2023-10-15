package com.tarantula.platform.service.persistence;

import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.service.cluster.PortableRegistry;

public class TransactionResult extends RecoverableObject {

    //public static final String LABEL = "transaction";
    public boolean committed;

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeBoolean(committed);

        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        committed = buffer.readBoolean();
        return true;
    }

    @Override
    public int getFactoryId() {
        return PortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PortableRegistry.TRANSACTION_RESULT_CID;
    }

    public static TransactionResult result(long transactionId,boolean committed){
        TransactionResult result = new TransactionResult();
        result.committed = committed;
        result.distributionId(transactionId);
        return result;
    }

}
