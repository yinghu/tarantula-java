package com.icodesoftware.lmdb.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.lmdb.LMDBDataStoreProvider;
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
    @Override
    public <T extends Recoverable> void onBackingUp(Metadata metadata, String key, T t) {

    }
    public boolean onRecovering(Metadata metadata, Recoverable.DataBuffer key, DataStore.BufferStream bufferStream){
        return false;
    }

    @Override
    public void onDistributing(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value) {
        //System.out.println(metadata.toString());
        if(metadata.label()!=null){
            List<BinaryKey> keys = new ArrayList<>();

            DataStore dataStore = provider.createDataStore(metadata.source());
            dataStore.backup().list(new BinaryKey(key.array()),metadata.label(),(k, h, v)->{
                //System.out.println("EDGE->From ["+metadata.label());
                keys.add(new BinaryKey(v.array()));
                return true;
            });

            keys.forEach(p->{
                dataStore.load(p,(k,h,v)->{
                    if(h.classId()==10){
                        TestUser testUser = new TestUser();
                        testUser.read(v);
                        //System.out.println(testUser.login());
                    }
                    return true;
                });
            });
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
    public byte[] onRecovering(Metadata metadata, String stringKey, byte[] key) {

        return null;
    }

    @Override
    public void onDeleting(Metadata metadata, byte[] key) {

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
