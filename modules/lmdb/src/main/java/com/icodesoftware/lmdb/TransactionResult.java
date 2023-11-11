package com.icodesoftware.lmdb;

import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.SnowflakeKey;

public class TransactionResult extends RecoverableObject {

    public static final String LABEL = "transaction_result";
    public boolean committed;
    public int scope;
    public boolean replicated;

    public TransactionResult(){
        this.label = LABEL;
        this.onEdge = true;
    }
    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeBoolean(committed);
        buffer.writeInt(scope);
        buffer.writeBoolean(replicated);
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        committed = buffer.readBoolean();
        scope = buffer.readInt();
        replicated = buffer.readBoolean();
        return true;
    }

    @Override
    public int getFactoryId() {
        return PersistencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PersistencePortableRegistry.TRANSACTION_RESULT_CID;
    }

    public static TransactionResult result(long transactionId,int scope,boolean committed,long nodeId){

        TransactionResult result = new TransactionResult();
        result.committed = committed;
        result.scope = scope;
        result.distributionId(transactionId);
        result.ownerKey(new SnowflakeKey(nodeId));
        return result;
    }

}
