package com.tarantula.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.service.*;

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


    public void onDistributing(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value,long transactionId){
        DataStore dataStore = dataStoreProvider.createLogDataStore("log_"+metadata.source());
        //System.out.println("TRANSACTION ID : "+transactionId+" : "+metadata.source()+" : "+dataStore.name()+" : "+metadata.label());
        if(metadata.label()==null){
            Recoverable.DataHeader header = value.readHeader();
            value.rewind();
            boolean suc = dataStore.backup().set((k,v)->{
                for(byte b : key.array()){
                    k.writeByte(b);
                }
                for(byte b : value.array()){
                    v.writeByte(b);
                }
                return true;
            });
            //System.out.println("HD : "+header.factoryId()+" : "+header.classId()+" : "+header.revision()+" : "+suc+" : "+metadata.source());
            return;
        }
        boolean suc = dataStore.backup().setEdge(metadata.label(),(k,v)->{
            for(byte b : key.array()){
                k.writeByte(b);
            }
            for(byte b : value.array()){
                v.writeByte(b);
            }
            return true;
        });
        //System.out.println("EG : "+metadata.label()+" : "+suc+" : "+metadata.source());
    }


    public boolean onRecovering(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer bufferStream){
        return false;
    }
    @Override
    public boolean onDeleting(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value,long transactionId) {
         System.out.println("DEL : "+metadata.source()+" : "+metadata.label());
         return true;
    }
}
