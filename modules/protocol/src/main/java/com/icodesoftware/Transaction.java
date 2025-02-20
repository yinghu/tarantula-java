package com.icodesoftware;

import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.Metadata;
import com.icodesoftware.service.ServiceContext;

import java.util.List;

public interface Transaction extends AutoCloseable{

    boolean execute(TransactionContext transactionContext);

    @Override
    void close();

    void register(DataStoreContext dataStoreContext, Listener listener);

    interface TransactionContext{
        boolean update(DataStoreContext dataStoreContext);
    }
    interface DataStoreContext extends Closable{
        default void parent(DataStoreContext parentContext){}
        default DataStore onDataStore(String name){ return null;}

    }
    interface Listener{
        default void afterCommit(long transactionId){}
        default void afterAbort(long transactionId,Exception exception){}
    }

    interface Log{
        boolean deleting();
        int sourceScope();
        String source();
        String edgeLabel();
        long revisionNumber();
        byte[] primaryKey();
        byte[] value();
        byte[] edgeKey();

        void deleting(boolean deleting);
        void sourceScope(int sourceScope);
        void source(String source);
        void edgeLabel(String edgeLabel);
        void revisionNumber(long revisionNumber);
        void primaryKey(byte[] primaryKey);
        void value(byte[] value);
        void edgeKey(byte[] edgeKey);

        byte[] toBinary();
        void fromBinary(byte[] payload);

         boolean read(Recoverable.DataBuffer buffer);
         boolean write(Recoverable.DataBuffer buffer);
    }

    interface History extends Recoverable{
        int scope();
        boolean committed();
        long transactionId();
    }

    interface LogManager{
        void setup(ServiceContext serviceContext);

        //callback on local transaction commit
        void onCommit(int scope, long transactionId);

        //callback on local transaction abort
        void onAbort(int scope, long transactionId);

        //local transaction logs per transaction
        List<Log> committed(int scope, long transactionId);

        //callback on transaction replay
        void registerLogListener(Transaction.LogListener listener);

        //local transaction replay per transaction
        void onTransaction(List<Transaction.Log> transactionLogs);

        //callback on store set
        void onUpdating(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value, long transactionId);

        //callback on store delete
        boolean onDeleting(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value, long transactionId);

        //callback on store load
        boolean onRecovering(Metadata metadata,Recoverable.DataBuffer key,DataStore.BufferStream bufferStream);

        List<History> history(int scope,ClusterProvider.Node node);
        void history(int scope, ClusterProvider.Node node, DataStore.Stream<History> stream);
    }

    interface LogListener {
        void onLog(Transaction.Log transactionLog);
    }
}
