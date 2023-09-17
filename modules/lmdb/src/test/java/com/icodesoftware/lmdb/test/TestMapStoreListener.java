package com.icodesoftware.lmdb.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.lmdb.LMDBDataStoreProvider;
import com.icodesoftware.service.AccessIndexService;
import com.icodesoftware.service.KeyIndexService;
import com.icodesoftware.service.MapStoreListener;
import com.icodesoftware.service.Metadata;
import com.icodesoftware.util.BinaryKey;

import java.util.ArrayList;
import java.util.List;

public class TestMapStoreListener implements MapStoreListener {

    LMDBDataStoreProvider provider;

    public TestMapStoreListener(LMDBDataStoreProvider provider){
        this.provider = provider;
    }

    public boolean onRecovering(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value){
        DataStore dataStore = provider.createKeyIndexDataStore(KeyIndexService.KeyIndexStore.STORE_NAME+"_"+metadata.source());
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
        dataStore.backup().forEachEdgeKey(new BinaryKey(key.array()),metadata.label(),(k,v)->{
           if(src.backup().setEdge(metadata.label(),(x,y)->{
               for(byte b : k.array()){
                   x.writeByte(b);
               }
               for(byte b : v.array()){
                   y.writeByte(b);
               }
               return true;
           })) suc[0]++;
           return true;
        });
        return suc[0]>0;
    }

    @Override
    public void onDistributing(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value) {
        DataStore dataStore = provider.createKeyIndexDataStore(KeyIndexService.KeyIndexStore.STORE_NAME + "_" + metadata.source());
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
    public void onDeleting(Metadata metadata,Recoverable.DataBuffer key) {

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
