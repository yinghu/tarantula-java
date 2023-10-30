package com.tarantula.platform.service.persistence;

import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.service.Metadata;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.BinaryKey;

import java.util.List;

public class TransactionLogManager{

    private static final String DATA_PREFIX = "log_d_";
    private static final String ACCESS_PREFIX = "log_a_";
    private static final String INDEX_PREFIX = "log_i_";
    private static final String TRANSACTION_LOG = "log_tarantula_transaction";

    private ServiceContext serviceContext;

    public void setup(ServiceContext serviceContext){
        this.serviceContext = serviceContext;
    }
    public List<TransactionLog> committed(long transactionId){
        return null;
    }


    public void onUpdating(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value, long transactionId) {
        DataStore dataStore = serviceContext.dataStore(Distributable.LOG_SCOPE,logPrefix(metadata.scope())+metadata.source());
        DataStore ts = serviceContext.dataStore(Distributable.LOG_SCOPE,TRANSACTION_LOG);
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
        //System.out.println("EG : "+metadata.label()+" : "+suc+" : "+metadata.source());
    }


    public boolean onRecovering(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value) {
        DataStore dataStore = serviceContext.dataStore(Distributable.LOG_SCOPE,logPrefix(metadata.scope())+metadata.source());
        if(metadata.label()==null){

            return false;
        }
        //dataStore.backup().forEachEdgeKey();
        return false;
    }


    public boolean onDeleting(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value, long transactionId) {
        System.out.println("DEL : "+metadata.source()+" : "+metadata.label());
        DataStore dataStore = serviceContext.dataStore(Distributable.LOG_SCOPE,logPrefix(metadata.scope())+metadata.source());
        DataStore ts = serviceContext.dataStore(Distributable.LOG_SCOPE,TRANSACTION_LOG);
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
        DataStore ts = serviceContext.dataStore(Distributable.LOG_SCOPE,TRANSACTION_LOG);
        TransactionLogQuery query = new TransactionLogQuery(transactionId);
        ts.list(query).forEach(t->{
            System.out.println("Committed : "+transactionId+" : "+t.distributionId()+" : "+t.source+" : "+t.edgeLabel+" : "+t.scope+" : "+t.deleting+" : "+t.updatingRevision);
        });
        ts.createIfAbsent(TransactionResult.result(transactionId,true),false);
    }


    public void onAbort(int scope, long transactionId) {
        System.out.println("Aborted : "+transactionId+" : "+scope);
        DataStore ts = serviceContext.dataStore(Distributable.LOG_SCOPE,TRANSACTION_LOG);
        ts.createIfAbsent(TransactionResult.result(transactionId,false),false);
    }

    private String logPrefix(int scope){
        if(scope==Distributable.DATA_SCOPE) return DATA_PREFIX;
        if(scope==Distributable.INTEGRATION_SCOPE) return ACCESS_PREFIX;
        if(scope==Distributable.INDEX_SCOPE) return INDEX_PREFIX;
        return "log_";
    }

}
