package com.icodesoftware.lmdb.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.lmdb.LMDBDataStoreProvider;
import com.icodesoftware.service.KeyIndexService;
import com.icodesoftware.service.MapStoreListener;
import com.icodesoftware.service.Metadata;
import com.icodesoftware.util.BinaryKey;

import java.util.ArrayList;


public class TestMapStoreListener implements MapStoreListener {

    LMDBDataStoreProvider provider;

    public TestMapStoreListener(LMDBDataStoreProvider provider){
        this.provider = provider;
    }

    public boolean onRecovering(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value,DataStore.BufferStream bufferStream){
        DataStore dataStore = provider.createKeyIndexDataStore(KeyIndexService.STORE_NAME+"data_user");
        if(metadata.label()==null){
            byte[] kbs = key.array();
            return dataStore.backup().get(new BinaryKey(kbs),(k, v)->{
                for(byte b :v.array()){
                    value.writeByte(b);
                }
                return true;
            });
        }
        DataStore src = provider.lookup(metadata.source());
        int[] suc = {0};
        ArrayList<BinaryKey> klist = new ArrayList<>();
        byte[] arr = key.array();
        dataStore.backup().forEachEdgeKey(new BinaryKey(arr),metadata.label(),(k,v)->{
            if(src.backup().setEdge(metadata.label(),(x,y)->{
               for(byte b : arr){
                   x.writeByte(b);
               }
               byte[] vb = v.array();
               klist.add(new BinaryKey(vb));
               for(byte b : vb){
                   y.writeByte(b);
               }
               return true;
            })) suc[0]++;
            return true;
        });
        klist.forEach(k->
            dataStore.backup().get(k,(mk,mv)->{
                src.backup().set((tk,tv)->{
                    for(byte b :k.key){
                        tk.writeByte(b);
                    }
                    for(byte b :mv.array()){
                        tv.writeByte(b);
                    }
                    return true;
                });
                return true;
            })
        );
        return suc[0]>0;
    }

    @Override
    public void onUpdating(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value,long transactionId) {
        DataStore dataStore = provider.createKeyIndexDataStore(KeyIndexService.STORE_NAME + metadata.source());
        if(metadata.label()==null){
            dataStore.backup().set((k,v)->{
                for(byte b : key.array()){
                    k.writeByte(b);
                }
                for(byte b : value.array()){
                    v.writeByte(b);
                }
                return true;
            });
            return;
        }
        dataStore.backup().setEdge(metadata.label(),(k,v)->{
            for(byte b : key.array()){
                k.writeByte(b);
            }
            for(byte b : value.array()){
                v.writeByte(b);
            }
            return true;
        });

    }

    @Override
    public void onCommit(int scope,long transactionId) {
        //System.out.println("DB Commit->"+scope+" : "+transactionId);
    }

    @Override
    public void onAbort(int scope,long transactionId) {
        //System.out.println("DB Abort->"+scope+" : "+transactionId);
    }

    @Override
    public boolean onDeleting(Metadata metadata,Recoverable.DataBuffer key, Recoverable.DataBuffer value,long transactionId) {
        DataStore dataStore = provider.createKeyIndexDataStore(KeyIndexService.STORE_NAME +  metadata.source());
        if(metadata.label()==null){
            boolean del = dataStore.backup().unset((k,v)->{
                for(byte b : key.array()){
                    k.writeByte(b);
                }
                return true;
            });
            System.out.println("DEL :"+metadata.source()+":"+dataStore.name()+":"+del);
            return true;
        }
        boolean del = dataStore.backup().unsetEdge(metadata.label(),(k,v)->{
            for(byte b : key.array()){
                k.writeByte(b);
            }
            if(value==null) return true;
            for(byte b : value.array()){
                v.writeByte(b);
            }
            return true;
        },value==null);
        System.out.println("UNSET EDGE :"+metadata.label()+":"+del);
        return true;
    }

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
}
