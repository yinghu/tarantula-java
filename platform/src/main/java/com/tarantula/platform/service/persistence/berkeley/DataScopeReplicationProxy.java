package com.tarantula.platform.service.persistence.berkeley;

import com.icodesoftware.Recoverable;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.Metadata;
import com.tarantula.platform.service.DataStoreProvider;
import com.tarantula.platform.service.persistence.RevisionObject;

public class DataScopeReplicationProxy extends ScopedReplicationProxy {
    private TarantulaLogger logger = JDKLogger.getLogger(DataScopeReplicationProxy.class);

    public DataScopeReplicationProxy(DataStoreProvider dataStoreProvider){
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

    }

    @Override
    public byte[] onRecovering(Metadata metadata, String stringKey, byte[] key) {
        return new byte[0];
    }

    @Override
    public void onDeleting(Metadata metadata, byte[] key) {

    }


}
