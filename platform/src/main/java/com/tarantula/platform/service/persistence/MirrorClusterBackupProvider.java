package com.tarantula.platform.service.persistence;

import com.icodesoftware.Recoverable;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.BackupProvider;
import com.icodesoftware.service.Metadata;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.service.DataStoreProvider;

import java.util.Map;

public class MirrorClusterBackupProvider implements BackupProvider {

    private JDKLogger logger = JDKLogger.getLogger(MirrorClusterBackupProvider.class);
    private DataStoreProvider dataStoreProvider;

    public MirrorClusterBackupProvider(DataStoreProvider dataStoreProvider){
        this.dataStoreProvider = dataStoreProvider;
    }
    @Override
    public boolean enabled() {
        return false;
    }

    @Override
    public void enabled(boolean enabled) {

    }

    @Override
    public int scope() {
        return 0;
    }

    @Override
    public void registerDataStore(String name) {
        this.dataStoreProvider.create(name);
    }

    @Override
    public void registerDataStore(String prefix, int partitions) {
        this.dataStoreProvider.create(prefix,partitions);
    }

    @Override
    public void configure(Map<String, Object> properties) {

    }

    @Override
    public <T extends Recoverable> void update(Metadata metadata, String key, T t) {
        logger.warn("register 3->"+key);
    }

    @Override
    public <T extends Recoverable> void create(Metadata metadata, String key, T t) {
        logger.warn("register 4->"+key);
    }

    @Override
    public void update(Metadata metadata, String key, byte[] t) {
        logger.warn("register 5->"+key);
    }

    @Override
    public  void create(Metadata metadata, String key, byte[] t) {
        logger.warn("register 6->"+key);
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

    public void setup(ServiceContext serviceContext){

    }
}
