package com.icodesoftware.lmdb.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.lmdb.LMDBDataStoreProvider;
import com.icodesoftware.service.AccessIndexService;
import com.icodesoftware.service.MapStoreListener;
import com.icodesoftware.service.Metadata;
import com.icodesoftware.util.BinaryKey;


import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class TestMapStoreListener implements MapStoreListener {

    LMDBDataStoreProvider provider;

    public TestMapStoreListener(LMDBDataStoreProvider provider){
        this.provider = provider;
    }

    public boolean onRecovering(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value){
        DataStore dataStore = provider.createAccessIndexDataStore(AccessIndexService.AccessIndexStore.STORE_NAME+"_backup");
        byte[] kbs = key.array();
        return dataStore.backup().get(new BinaryKey(kbs),(k, v)->{
            for(byte b :v.array()){
                value.writeByte(b);
            }
            return true;
        });
    }

    @Override
    public void onDistributing(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value) {
        //System.out.println(metadata.toString());
        if(metadata.label()!=null){
            List<BinaryKey> keys = new ArrayList<>();

            DataStore dataStore = provider.createDataStore(metadata.source());
            dataStore.backup().forEachEdgeKey(new BinaryKey(key.array()),metadata.label(),(k, v)->{
                //System.out.println("EDGE->From ["+metadata.label());
                keys.add(new BinaryKey(v.array()));
                return true;
            });

            keys.forEach(p->
                dataStore.backup().get(p,(k,v)->{
                    Recoverable.DataHeader h = v.readHeader();
                    if(h.classId()==10){
                        TestUser testUser = new TestUser();
                        testUser.read(v);
                        //System.out.println(testUser.login());
                    }
                    return true;
                })
            );
        }


        //DataStore ds = provider.createDataStore("user_backup");
        //ds.backup().set((k,h,v)->{
            //for(byte b : key.array()){
                //k.writeByte(b);
            //}
            //for(byte b : value.array()){
                //v.writeByte(b);
            //}
            //return true;
        //});
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
