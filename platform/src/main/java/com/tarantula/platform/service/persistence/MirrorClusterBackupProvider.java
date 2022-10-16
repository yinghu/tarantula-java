package com.tarantula.platform.service.persistence;

import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.BackupProvider;
import com.icodesoftware.service.Metadata;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.service.DataStoreProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MirrorClusterBackupProvider implements BackupProvider{

    private JDKLogger log = JDKLogger.getLogger(MirrorClusterBackupProvider.class);

    private DataStoreProvider dataStoreProvider;
    private boolean runAsMirror;
    private ServiceContext serviceContext;
    private ConcurrentHashMap<String,BackupProvider> bMap;

    public MirrorClusterBackupProvider(DataStoreProvider dataStoreProvider){
        this.dataStoreProvider = dataStoreProvider;
    }

    @Override
    public boolean enabled() {
        return runAsMirror;
    }

    @Override
    public void enabled(boolean enabled) {
        this.runAsMirror = enabled;
    }

    @Override
    public int scope() {
        return 0;
    }

    @Override
    public void registerDataStore(String name) {
        if(runAsMirror){
            this.dataStoreProvider.create(name);
        }else {
            log.warn("run on master node->"+name);
        }

    }

    @Override
    public void registerDataStore(String prefix, int partitions) {
        if(runAsMirror) {
            this.dataStoreProvider.create(prefix, serviceContext.partitionNumber());
        }else{
            log.warn("run on master node->"+prefix+">>"+partitions);
        }
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
            byte[] r = RevisionObject.toBinary(metadata.revision(),t,true);
            this.dataStoreProvider.create(metadata.source(),serviceContext.partitionNumber()).backup().set(key.getBytes(),r);
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
            byte[] r = RevisionObject.toBinary(metadata.revision(),t,true);
            this.dataStoreProvider.create(metadata.source(),serviceContext.partitionNumber()).backup().set(key.getBytes(),r);
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
        this.bMap = new ConcurrentHashMap<>();
    }

    public void addBackupProvider(BackupProvider backupProvider){
        bMap.put(backupProvider.name(),backupProvider);
    }
    public void removeBackupProvider(BackupProvider backupProvider){
        bMap.remove(backupProvider.name());
    }
}
