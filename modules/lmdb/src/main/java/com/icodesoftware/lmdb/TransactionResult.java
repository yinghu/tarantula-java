package com.icodesoftware.lmdb;

import com.icodesoftware.Transaction;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.SnowflakeKey;

public class TransactionResult extends RecoverableObject implements Transaction.History {

    public static final String LABEL = "transaction_result";
    public boolean committed;
    public int scope;
    public long transactionId;
    public TransactionResult(){
        this.label = LABEL;
        this.onEdge = true;
    }
    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeLong(transactionId);
        buffer.writeBoolean(committed);
        buffer.writeInt(scope);

        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        transactionId = buffer.readLong();
        committed = buffer.readBoolean();
        scope = buffer.readInt();
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
        result.transactionId= transactionId;
        result.ownerKey(new SnowflakeKey(nodeId));
        return result;
    }

    @Override
    public int scope() {
        return scope;
    }

    @Override
    public boolean committed() {
        return committed;
    }

    @Override
    public long transactionId() {
        return transactionId;
    }
}
