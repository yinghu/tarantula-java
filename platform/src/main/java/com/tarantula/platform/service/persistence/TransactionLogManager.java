package com.tarantula.platform.service.persistence;

import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.service.Metadata;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.BinaryKey;

import java.util.List;

public class TransactionLogManager{

    private static final String STORE_PREFIX = "log_";
    private static final String TRANSACTION_LOG = "log_tarantula_transaction";

    private ServiceContext serviceContext;

    public void setup(ServiceContext serviceContext){
        this.serviceContext = serviceContext;
    }
    public List<TransactionLog> committed(long transactionId){
        return null;
    }


    public void onDistributing(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value, long transactionId) {
        DataStore dataStore = serviceContext.dataStore(Distributable.LOG_SCOPE,STORE_PREFIX+metadata.source());
        DataStore ts = serviceContext.dataStore(Distributable.LOG_SCOPE,"log_trx");
        if(metadata.label()==null){
            Recoverable.DataHeader header = value.readHeader();
            value.rewind();
            byte[] ak = key.array();
            byte[] av = value.array();
            boolean suc = dataStore.backup().set((k,v)->{
                for(byte b : ak){
                    k.writeByte(b);
                }
                for(byte b : av){
                    v.writeByte(b);
                }
                return true;
            });
            if(!suc) return;
            TransactionLog log = TransactionLog.log(transactionId,false, metadata.scope(), metadata.source(),metadata.label(),ak,null,header.revision());
            ts.create(log);
            return;
        }
        byte[] ak = key.array();
        byte[] av = value.array();
        boolean suc = dataStore.backup().setEdge(metadata.label(),(k,v)->{
            for(byte b : ak){
                k.writeByte(b);
            }
            for(byte b : av){
                v.writeByte(b);
            }
            return true;
        });
        if(!suc) return;
        TransactionLog log = TransactionLog.log(transactionId,false,metadata.scope(),metadata.source(),metadata.label(),ak,av,0);
        ts.create(log);
        System.out.println("EG : "+metadata.label()+" : "+suc+" : "+metadata.source());
    }


    public boolean onRecovering(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value) {
        return false;
    }


    public boolean onDeleting(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value, long transactionId) {
        System.out.println("DEL : "+metadata.source()+" : "+metadata.label());
        DataStore dataStore = serviceContext.dataStore(Distributable.LOG_SCOPE,"log_"+metadata.source());
        DataStore ts = serviceContext.dataStore(Distributable.LOG_SCOPE,"log_trx");
        if(metadata.label()==null){
            byte[] ak = key.array();
            if(!dataStore.backup().unset((k,v)->{
                for(byte b : ak){
                    k.writeByte(b);
                }
                return true;
            })) return false;
            TransactionLog log = TransactionLog.log(transactionId,true, metadata.scope(), metadata.source(),metadata.label(),ak,null,0);
            ts.create(log);
            return true;
        }
        if(value!=null) {
            byte[] ak = key.array();
            byte[] av = value.array();
            TransactionLog log = TransactionLog.log(transactionId,true, metadata.scope(), metadata.source(),metadata.label(),ak,av,0);
            ts.create(log);
            return dataStore.deleteEdge(new BinaryKey(ak),new BinaryKey(av), metadata.label());
        }
        byte[] ak = key.array();
        TransactionLog log = TransactionLog.log(transactionId,true, metadata.scope(), metadata.source(),metadata.label(),ak,null,0);
        ts.create(log);
        return dataStore.deleteEdge(new BinaryKey(ak),metadata.label());
    }


    public void onCommit(int scope, long transactionId) {
        DataStore ts = serviceContext.dataStore(Distributable.LOG_SCOPE,"log_trx");
        TransactionLogQuery query = new TransactionLogQuery(transactionId);
        ts.list(query).forEach(t->{
            System.out.println("Committed : "+transactionId+" : "+t.distributionId()+" : "+t.source+" : "+t.edgeLabel+" : "+t.scope+" : "+t.deleting+" : "+t.updatingRevision);
        });
        ts.createIfAbsent(TransactionResult.result(transactionId,true),false);
    }


    public void onAbort(int scope, long transactionId) {
        System.out.println("Aborted : "+transactionId+" : "+scope);
        DataStore ts = serviceContext.dataStore(Distributable.LOG_SCOPE,"log_trx");
        ts.createIfAbsent(TransactionResult.result(transactionId,false),false);
    }

}
