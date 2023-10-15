package com.tarantula.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.service.*;

import com.icodesoftware.util.BinaryKey;
import com.tarantula.platform.service.persistence.TransactionLog;
import com.tarantula.platform.service.persistence.TransactionLogQuery;

public class TestMapStoreListener implements MapStoreListener {

    DataStoreProvider dataStoreProvider;
     @Override
    public String name() {
        return null;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }


    public void onDistributing(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value,long transactionId){
        DataStore dataStore = dataStoreProvider.createLogDataStore("log_"+metadata.source());
        DataStore ts = dataStoreProvider.createLogDataStore("log_trx");
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
            TransactionLog log = TransactionLog.log(transactionId,false, metadata.scope(), metadata.source(),metadata.label(),ak,null);
            ts.create(log);
            /**
            boolean CUS = dataStore.backup().setEdge("transaction",(k,v)->{
                k.writeLong(transactionId);
                key.rewind();
                for(byte b : key.array()){
                    v.writeByte(b);
                }
                return true;
            });
            int[] ct={0};
            dataStore.backup().forEachEdgeKey(new SnowflakeKey(transactionId),"transaction",(k,v)->{
                ct[0]++;
                return true;
            });**/
            //System.out.println("HD : "+header.factoryId()+" : "+header.classId()+" : "+header.revision()+" : "+suc+" : "+metadata.source()+" : "+CUS+" : "+ct[0]);

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
        TransactionLog log = TransactionLog.log(transactionId,false,metadata.scope(),metadata.source(),metadata.label(),ak,av);
        ts.create(log);
        System.out.println("EG : "+metadata.label()+" : "+suc+" : "+metadata.source());

    }
    @Override
    public void onCommit(int scope,long transactionId) {
        DataStore ts = dataStoreProvider.createLogDataStore("log_trx");
        TransactionLogQuery query = new TransactionLogQuery(transactionId);
        ts.list(query).forEach(t->{
            System.out.println("Committed : "+transactionId+" : "+t.distributionId()+" : "+t.source+" : "+t.edgeLabel+" : "+t.scope+" : "+t.deleting);
        });
    }

    @Override
    public void onAbort(int scope,long transactionId) {

    }

    public boolean onRecovering(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer bufferStream){
        return false;
    }
    @Override
    public boolean onDeleting(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value,long transactionId) {
         System.out.println("DEL : "+metadata.source()+" : "+metadata.label());
         DataStore dataStore = dataStoreProvider.createLogDataStore("log_"+metadata.source());
         DataStore ts = dataStoreProvider.createLogDataStore("log_trx");
         if(metadata.label()==null){
             byte[] ak = key.array();
             if(!dataStore.backup().unset((k,v)->{
                 for(byte b : ak){
                     k.writeByte(b);
                 }
                 return true;
             })) return false;
             TransactionLog log = TransactionLog.log(transactionId,true, metadata.scope(), metadata.source(),metadata.label(),ak,null);
             ts.create(log);
             return true;
         }
         if(value!=null) {
             byte[] ak = key.array();
             byte[] av = value.array();
             TransactionLog log = TransactionLog.log(transactionId,true, metadata.scope(), metadata.source(),metadata.label(),ak,av);
             ts.create(log);
             return dataStore.deleteEdge(new BinaryKey(ak),new BinaryKey(av), metadata.label());
         }
         byte[] ak = key.array();
         TransactionLog log = TransactionLog.log(transactionId,true, metadata.scope(), metadata.source(),metadata.label(),ak,null);
         ts.create(log);
         return dataStore.deleteEdge(new BinaryKey(ak),metadata.label());

    }
}
