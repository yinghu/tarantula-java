package com.tarantula.platform.service.persistence;

import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.service.cluster.PortableRegistry;

public class TransactionResult extends RecoverableObject {

    //public static final String LABEL = "transaction";
    public boolean committed;
    public int scope;
    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeBoolean(committed);
        buffer.writeInt(scope);
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        committed = buffer.readBoolean();
        scope = buffer.readInt();
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

    public static TransactionResult result(long transactionId,int scope,boolean committed){
        TransactionResult result = new TransactionResult();
        result.committed = committed;
        result.scope = scope;
        result.distributionId(transactionId);
        return result;
    }

}
