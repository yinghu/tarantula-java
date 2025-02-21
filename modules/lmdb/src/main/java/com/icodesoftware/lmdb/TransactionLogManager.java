package com.icodesoftware.lmdb;

import com.icodesoftware.*;
import com.icodesoftware.service.Batchable;
import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.Metadata;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.BinaryKey;
import com.icodesoftware.util.BufferProxy;
import com.icodesoftware.util.DataBufferKey;

import java.util.ArrayList;
import java.util.List;

public class TransactionLogManager implements Transaction.LogManager{


    public static final String DATA_PREFIX = "log_d_";
    public static final String ACCESS_PREFIX = "log_a_";
    public static final String INDEX_PREFIX = "log_i_";

    public static final String DATA_PREFIX_I = "index_d_";
    public static final String ACCESS_PREFIX_I = "index_a_";

    public static final String INDEX_PREFIX_I = "index_i_";
    public static final String TRANSACTION_LOG = "log_tarantula_transaction";

    public static final String DATA_TRANSACTION_LOG = "log_tarantula_transaction_1";
    public static final String INTEGRATION_TRANSACTION_LOG = "log_tarantula_transaction_2";

    private Transaction.LogListener transactionLogListener = transactionLog -> {};
    private ServiceContext serviceContext;

    public void setup(ServiceContext serviceContext){
        this.serviceContext = serviceContext;
    }

    public void registerLogListener(Transaction.LogListener listener){
        if(listener==null) return;
        this.transactionLogListener = listener;
    }
    public List<Transaction.Log> committed(int scope,long transactionId){
        DataStore ts = transactionLogStore(scope);
        TransactionLogQuery query = new TransactionLogQuery(transactionId);
        List<Transaction.Log> pending = new ArrayList<>();
        ts.list(query).forEach(t->{
            DataStore tds = serviceContext.dataStore(Distributable.LOG_SCOPE,logPrefix(t.sourceScope())+t.source());
            if(t.edgeLabel()==null && !t.deleting()){
                tds.backup().get(BinaryKey.from(t.primaryKey()),(k, v)->{
                    t.value(v.array());
                    pending.add(t);
                    return true;
                });
            }else{
                pending.add(t);
            }
        });
        return pending;
    }

    public List<Transaction.History> history(int scope, ClusterProvider.Node node){
        DataStore ts = transactionLogStore(scope);
        TransactionResultQuery query = new TransactionResultQuery(node.nodeId());
        return ts.list(query);
    }

    public void history(int scope, ClusterProvider.Node node, DataStore.Stream<Transaction.History> stream){
        DataStore ts = transactionLogStore(scope);
        TransactionResultQuery query = new TransactionResultQuery(node.nodeId());
        ts.list(query,stream);
    }

    public void onUpdating(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value, long transactionId) {
        DataStore dataStore = serviceContext.dataStore(Distributable.LOG_SCOPE,logPrefix(metadata.scope())+metadata.source());
        DataStore ts = transactionLogStore(metadata.scope());
        if(metadata.label()==null){
            Recoverable.DataHeader header = value.readHeader();
            value.rewind();
            boolean suc = dataStore.backup().set((k,v)->{
                k.write(key);
                v.write(value);
                return true;
            });
            if(!suc || transactionId <0 ) return;
            key.rewind();
            TransactionLog log = TransactionLog.log(transactionId,false, metadata.scope(), metadata.source(),metadata.label(),key.array(),null,header.revision());
            ts.create(log);
            return;
        }
        boolean suc = dataStore.backup().setEdge(metadata.label(),(k,v)->{
            k.write(key);
            v.write(value);
            return true;
        });
        if(!suc || transactionId <0 ) return;
        key.rewind();
        value.rewind();
        TransactionLog log = TransactionLog.log(transactionId,false,metadata.scope(),metadata.source(),metadata.label(),key.array(),value.array(),0);
        ts.create(log);
    }


    public boolean onRecovering(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value) {
        DataStore dataStore = serviceContext.dataStore(Distributable.INDEX_SCOPE,indexPrefix(metadata.scope())+metadata.source());
        if(metadata.label()!=null) return false;
        return dataStore.backup().get(BinaryKey.from(key.array()),(k,v)->{
            value.write(v);
            return true;
        });
    }

    public byte[] loadFromCommitted(Metadata metadata,byte[] key){
        DataStore dataStore = serviceContext.dataStore(Distributable.INDEX_SCOPE,indexPrefix(metadata.scope())+metadata.source());
        if(metadata.label()!=null) return null;
        byte[][] loaded = new byte[1][];
        if(dataStore.backup().get(BinaryKey.from(key),(k,v)->{
            loaded[0] = v.array();
            return true;
        })) return loaded[0];
        return null;
    }

    public Recoverable.DataBuffer get(Metadata metadata, Recoverable.DataBuffer key){
        DataStore dataStore = serviceContext.dataStore(Distributable.INDEX_SCOPE,indexPrefix(metadata.scope())+metadata.source());
        if(metadata.label()!=null) return null;
        Recoverable.DataBuffer[] loaded = {null};
        if(dataStore.backup().get(DataBufferKey.from(key),(k, v)->{
            loaded[0] = BufferProxy.copy(v.src());
            return true;
        })) return loaded[0];
        return null;
    }

    public List<Batchable.BatchData> loadEdgeValueFromCommitted(Metadata metadata, byte[] key){
        DataStore dataStore = serviceContext.dataStore(Distributable.INDEX_SCOPE,indexPrefix(metadata.scope())+metadata.source());
        List<Batchable.BatchData> edgeValueSet = new ArrayList<>();
        if(metadata.label()==null) return edgeValueSet;
        dataStore.backup().forEachEdgeKeyValue(BinaryKey.from(key),metadata.label(),(e,v)->{
            edgeValueSet.add(new EdgeValueSet(e.array(),v.array()));
            return true;
        });
        return edgeValueSet;
    }


    public boolean onRecovering(Metadata metadata,Recoverable.DataBuffer key,DataStore.BufferStream bufferStream){
        DataStore dataStore = serviceContext.dataStore(Distributable.INDEX_SCOPE,indexPrefix(metadata.scope())+metadata.source());
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
            if(!dataStore.backup().unset((k,v)->{
                k.write(key);
                return true;
            })) return false;
            key.rewind();
            TransactionLog log = TransactionLog.log(transactionId,true, metadata.scope(), metadata.source(),metadata.label(),key.array(),null,0);
            ts.create(log);
            return true;
        }
        if(value!=null) {
            TransactionLog log = TransactionLog.log(transactionId,true, metadata.scope(), metadata.source(),metadata.label(),key.array(),value.array(),0);
            ts.create(log);
            key.rewind();
            value.rewind();
            return dataStore.deleteEdge(DataBufferKey.from(key),DataBufferKey.from(value), metadata.label());
        }
        TransactionLog log = TransactionLog.log(transactionId,true, metadata.scope(), metadata.source(),metadata.label(),key.array(),null,0);
        ts.create(log);
        key.rewind();
        return dataStore.deleteEdge(DataBufferKey.from(key),metadata.label());
    }



    public void onCommit(int scope, long transactionId) {
        DataStore ts = transactionLogStore(scope);
        ts.create(TransactionResult.result(transactionId,scope,true,serviceContext.node().nodeId()));
    }


    public void onAbort(int scope, long transactionId) {
        DataStore ts = transactionLogStore(scope);
        ts.create(TransactionResult.result(transactionId,scope,false,serviceContext.node().nodeId()));
    }

    private String logPrefix(int scope){
        if(scope==Distributable.DATA_SCOPE) return DATA_PREFIX;
        if(scope==Distributable.INTEGRATION_SCOPE) return ACCESS_PREFIX;
        if(scope==Distributable.INDEX_SCOPE) return INDEX_PREFIX;
        return "log_";
    }
    private String indexPrefix(int scope){
        if(scope==Distributable.DATA_SCOPE) return DATA_PREFIX_I;
        if(scope==Distributable.INTEGRATION_SCOPE) return ACCESS_PREFIX_I;
        if(scope==Distributable.INDEX_SCOPE) return INDEX_PREFIX_I;
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

    public DataStore onTransaction(Metadata metadata){
        return serviceContext.dataStore(Distributable.INDEX_SCOPE,indexPrefix(metadata.scope())+metadata.source());
    }

    public void onTransaction(List<Transaction.Log> transactionLogs) {
        for(Transaction.Log log : transactionLogs){
            DataStore dataStore = serviceContext.dataStore(Distributable.INDEX_SCOPE,indexPrefix(log.sourceScope())+log.source());
            if(log.deleting()){
                if(log.edgeLabel()==null){
                    dataStore.backup().unset((k,v)->{
                        k.write(log.primaryKey());
                        return true;
                    });
                }else {
                    dataStore.backup().unsetEdge(log.edgeLabel(), (k, v) -> {
                        k.write(log.primaryKey());
                        if (log.edgeKey() == null) return true;
                        v.write(log.edgeKey());
                        return true;
                    }, log.edgeKey() == null);
                }
            }else{
                if(log.edgeLabel()==null){//write key/value
                    dataStore.backup().set((k,v)->{
                        k.write(log.primaryKey());
                        v.write(log.value());
                        return true;
                    });
                }else{
                    //write edge
                    dataStore.backup().setEdge(log.edgeLabel(),(k,v)->{
                        k.write(log.primaryKey());
                        v.write(log.edgeKey());
                        return true;
                    });
                }
            }
            transactionLogListener.onLog(log);
        }
    }


}
