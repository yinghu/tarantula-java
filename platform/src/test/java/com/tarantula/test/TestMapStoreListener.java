package com.tarantula.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.service.DataStoreProvider;
import com.icodesoftware.service.KeyIndex;
import com.icodesoftware.service.Metadata;
import com.icodesoftware.service.MapStoreListener;

import com.icodesoftware.util.BinaryKey;
import com.tarantula.platform.event.PortableEventRegistry;
import com.tarantula.platform.service.KeyIndexTrack;

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



    public void onDistributing(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value){
         /**
         Recoverable.DataHeader header = value.readHeader();
        if(header.factoryId()== PortableEventRegistry.OID && header.classId()== PortableEventRegistry.KEY_INDEX_CID){
            DataStore ds = dataStoreProvider.createKeyIndexDataStore(metadata.source());
            ds.backup().get(new BinaryKey(key.array()),(k,v)->{
                KeyIndex keyIndex = new KeyIndexTrack();
                keyIndex.readKey(k);
                Recoverable.DataHeader h = v.readHeader();
                keyIndex.read(v);
                System.out.println("PPPP->"+h.factoryId()+">>>"+h.classId()+">>"+keyIndex.masterNode());
                for(String s : keyIndex.slaveNodes()){
                    System.out.println(s);
                }
                return true;
            });
        }**/
    }


    public boolean onRecovering(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer bufferStream){
        return false;
    }
    @Override
    public void onDeleting(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value) {

    }
}
