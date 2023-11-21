package com.icodesoftware.lmdb;

import com.icodesoftware.Closable;
import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.service.Metadata;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.BinaryKey;

import java.util.ArrayList;
import java.util.List;

public class TransactionLogManager implements Closable {


    private static final String DATA_PREFIX = "log_d_";
    private static final String ACCESS_PREFIX = "log_a_";
    private static final String INDEX_PREFIX = "log_i_";
    private static final String TRANSACTION_LOG = "log_tarantula_transaction";

    private static final String DATA_TRANSACTION_LOG = "log_tarantula_transaction_1";
    private static final String INTEGRATION_TRANSACTION_LOG = "log_tarantula_transaction_2";

    private ServiceContext serviceContext;

    public void setup(ServiceContext serviceContext){
        this.serviceContext = serviceContext;
    }
    public List<TransactionLog> committed(int scope,long transactionId){
        DataStore ts = transactionLogStore(scope);
        TransactionLogQuery query = new TransactionLogQuery(transactionId);
        List<TransactionLog> pending = new ArrayList<>();
        ts.list(query).forEach(t->{
            DataStore tds = serviceContext.dataStore(Distributable.LOG_SCOPE,logPrefix(t.scope)+t.source);
            if(t.edgeLabel==null && !t.deleting){
                tds.backup().get(new BinaryKey(t.key),(k,v)->{
                    t.value = v.array();
                    pending.add(t);
                    return true;
                });
            }else{
                pending.add(t);
            }
        });
        return pending;
    }

    public List<TransactionResult> pending(int scopeId,long nodeId){
        DataStore ts = transactionLogStore(scopeId);
        TransactionResultQuery query = new TransactionResultQuery(nodeId);
        return ts.list(query);
    }

    public void onUpdating(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value, long transactionId) {
        DataStore dataStore = serviceContext.dataStore(Distributable.LOG_SCOPE,logPrefix(metadata.scope())+metadata.source());
        DataStore ts = transactionLogStore(metadata.scope());
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
    }


    public boolean onRecovering(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value) {
        DataStore dataStore = serviceContext.dataStore(Distributable.INDEX_SCOPE,logPrefix(metadata.scope())+metadata.source());
        if(metadata.label()!=null) return false;
        return dataStore.backup().get(BinaryKey.from(key.array()),(k,v)->{
            for(byte b : v.array()){
                value.writeByte(b);
            }
            return true;
        });

    }

    public boolean onRecovering(Metadata metadata,Recoverable.DataBuffer key,DataStore.BufferStream bufferStream){
        DataStore dataStore = serviceContext.dataStore(Distributable.INDEX_SCOPE,logPrefix(metadata.scope())+metadata.source());
        if(metadata.label()==null) return false;
        List<Recoverable.DataBuffer> ex = new ArrayList<>();
        List<Recoverable.DataBuffer> ev = new ArrayList<>();
        dataStore.backup().forEachEdgeKeyValue(BinaryKey.from(key.array()),metadata.label(),(e,v)->{
            ex.add(BufferProxy.wrapDirectly(e.array()));
            ev.add(BufferProxy.wrapDirectly(v.array()));
            return true;
        });
        key.rewind();
        int sz = ex.size();
        for(int i=0;i<ex.size();i++){
            bufferStream.on(ex.get(i),ev.get(i));
        }
        return sz>0;
    }


    public boolean onDeleting(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value, long transactionId) {
        DataStore dataStore = serviceContext.dataStore(Distributable.LOG_SCOPE,logPrefix(metadata.scope())+metadata.source());
        DataStore ts = transactionLogStore(metadata.scope());
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
        DataStore ts = transactionLogStore(scope);
        ts.createIfAbsent(TransactionResult.result(transactionId,scope,true,serviceContext.node().nodeId()),false);
    }


    public void onAbort(int scope, long transactionId) {
        DataStore ts = transactionLogStore(scope);
        ts.createIfAbsent(TransactionResult.result(transactionId,scope,false,serviceContext.node().nodeId()),false);
    }

    private String logPrefix(int scope){
        if(scope==Distributable.DATA_SCOPE) return DATA_PREFIX;
        if(scope==Distributable.INTEGRATION_SCOPE) return ACCESS_PREFIX;
        if(scope==Distributable.INDEX_SCOPE) return INDEX_PREFIX;
        return "log_";
    }

    private DataStore transactionLogStore(int scope){
        if(scope==Distributable.DATA_SCOPE){
            return serviceContext.dataStore(Distributable.LOG_SCOPE,DATA_TRANSACTION_LOG);
        }
        if(scope==Distributable.INTEGRATION_SCOPE){
            return serviceContext.dataStore(Distributable.LOG_SCOPE,INTEGRATION_TRANSACTION_LOG);
        }
        return serviceContext.dataStore(Distributable.LOG_SCOPE,TRANSACTION_LOG);
    }

    public void onTransaction(List<TransactionLog> transactionLogs) {
        for(TransactionLog log : transactionLogs){
            DataStore dataStore = serviceContext.dataStore(Distributable.INDEX_SCOPE,logPrefix(log.scope)+log.source);
            if(log.deleting){
                if(log.edgeLabel==null){
                    dataStore.backup().unset((k,v)->{
                        for(byte b : log.key){
                            k.writeByte(b);
                        }
                        return true;
                    });
                }else {
                    dataStore.backup().unsetEdge(log.edgeLabel, (k, v) -> {
                        for (byte b : log.key) {
                            k.writeByte(b);
                        }
                        if (log.edgeKey == null) return true;
                        for (byte b : log.edgeKey) {
                            v.writeByte(b);
                        }
                        return true;
                    }, log.edgeKey == null);
                }
            }else{
                if(log.edgeLabel==null){//write key/value
                    dataStore.backup().set((k,v)->{
                        for(byte b : log.key){
                            k.writeByte(b);
                        }
                        for(byte b : log.value){
                            v.writeByte(b);
                        }
                        return true;
                    });
                }else{
                    //write edge
                    dataStore.backup().setEdge(log.edgeLabel,(k,v)->{
                        for(byte b : log.key){
                            k.writeByte(b);
                        }
                        for(byte b : log.edgeKey){
                            v.writeByte(b);
                        }
                        return true;
                    });
                }
            }
        }
    }

    public void close(){
        //clear resources if any
    }
}
