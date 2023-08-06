package com.tarantula.platform.service.persistence.berkeley;

import com.icodesoftware.Recoverable;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.Metadata;
import com.tarantula.platform.service.DataStoreProvider;
import com.tarantula.platform.service.KeyIndexTrack;
import com.tarantula.platform.service.persistence.DataStoreOnPartition;
import com.tarantula.platform.service.persistence.RevisionObject;

public class IntegrationScopeReplicationProxy extends ScopedReplicationProxy {

    private TarantulaLogger logger = JDKLogger.getLogger(IntegrationScopeReplicationProxy.class);

    public IntegrationScopeReplicationProxy(DataStoreProvider dataStoreProvider){
        super(dataStoreProvider);
    }
    @Override
    public <T extends Recoverable> void onBackingUp(Metadata metadata, String key, T t) {

    }

    @Override
    public void onDistributing(Metadata metadata, String stringKey, byte[] key, byte[] value) {

    }

    @Override
    public void onDistributing(Metadata metadata, String stringKey, byte[] key, RevisionObject value) {
        logger.warn("distributing ["+stringKey+"]");
        //DataStoreOnPartition dso = onPartition(key);
        //dso.lock(key,()->{
          //      KeyIndexTrack keyIndex = new KeyIndexTrack();
            //    keyIndex.index(stringKey);
              //  keyIndex.placeMasterNode(new String(value.node));
               // return dso.dataStore.createIfAbsent(keyIndex,false);
            //}
        //);
    }

    @Override
    public byte[] onRecovering(Metadata metadata, String stringKey, byte[] key) {
        //DataStoreOnPartition dso = onPartition(key);
        //KeyIndexTrack keyIndexTrack = new KeyIndexTrack();
        //keyIndexTrack.index(stringKey);
        //if(dso.lock(key,()->dso.dataStore.load(keyIndexTrack))){
            //serviceContext.clusterProvider().accessIndexService().get(stringKey);
            //serviceContext.clusterProvider().accessIndexService().get()
            //return
        //}
        return null;
    }

    @Override
    public void onDeleting(Metadata metadata, byte[] key) {

    }



}
