package com.icodesoftware.lmdb.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.lmdb.LMDBDataStoreProvider;
import com.icodesoftware.service.MapStoreListener;
import com.icodesoftware.service.Metadata;


import java.nio.ByteBuffer;

public class TestMapStoreListener implements MapStoreListener {

    LMDBDataStoreProvider provider;

    public TestMapStoreListener(LMDBDataStoreProvider provider){
        this.provider = provider;
    }
    @Override
    public <T extends Recoverable> void onBackingUp(Metadata metadata, String key, T t) {

    }


    @Override
    public void onDistributing(Metadata metadata, ByteBuffer key, ByteBuffer value) {
        DataStore ds = provider.createDataStore("user_backup");
        ds.backup().set(key,value);
        //ds.backup().set((k,h,v)->{
            //k.writeUTF8("testkey");
            //v.writeUTF8("testvalue");
            //return true;
        //});
    }

    @Override
    public byte[] onRecovering(Metadata metadata, String stringKey, byte[] key) {
        return new byte[0];
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
