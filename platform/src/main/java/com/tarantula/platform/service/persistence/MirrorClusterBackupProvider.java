package com.tarantula.platform.service.persistence;

import com.icodesoftware.Distributable;
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
    private boolean enabled;
    private ServiceContext serviceContext;

    public MirrorClusterBackupProvider(DataStoreProvider dataStoreProvider){
        this.dataStoreProvider = dataStoreProvider;
    }

    @Override
    public boolean enabled() {
        return enabled;
    }

    @Override
    public void enabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public int scope() {
        return 0;
    }

    @Override
    public void registerDataStore(String name) {
        this.dataStoreProvider.create(name);
        //logger.warn("create data->"+name);
    }

    @Override
    public void registerDataStore(String prefix, int partitions) {
        this.dataStoreProvider.create(prefix,partitions);
        //logger.warn("create data->"+prefix+"//"+partitions);
    }

    @Override
    public void configure(Map<String, Object> properties) {

    }

    @Override
    public <T extends Recoverable> void update(Metadata metadata, String key, T t) {

    }

    @Override
    public <T extends Recoverable> void create(Metadata metadata, String key, T t) {

    }

    @Override
    public void update(Metadata metadata, String key, byte[] t) {
        if(metadata.scope()== Distributable.DATA_SCOPE){
            this.dataStoreProvider.create(metadata.source(),serviceContext.partitionNumber()).backup().set(key.getBytes(),t);
            return;
        }
        if(metadata.scope()==Distributable.INTEGRATION_SCOPE){
            this.dataStoreProvider.create(metadata.source()).backup().set(key.getBytes(),t);
            return;
        }
    }

    @Override
    public  void create(Metadata metadata, String key, byte[] t) {
        if(metadata.scope()== Distributable.DATA_SCOPE){
            this.dataStoreProvider.create(metadata.source(),serviceContext.partitionNumber()).backup().set(key.getBytes(),t);
            return;
        }
        if(metadata.scope()==Distributable.INTEGRATION_SCOPE){
            this.dataStoreProvider.create(metadata.source()).backup().set(key.getBytes(),t);
            return;
        }
    }

    @Override
    public String name() {
        return "MirrorClusterBackup";
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }

    public void setup(ServiceContext serviceContext){
        this.serviceContext = serviceContext;
    }
}
