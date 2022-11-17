package com.tarantula.platform.service.persistence;

import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.BackupProvider;
import com.icodesoftware.service.Metadata;
import com.icodesoftware.service.OnReplication;
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
            return;
        }
        BackupProvider backupProvider = bMap.get(_type(name));
        if(backupProvider == null) return;
        backupProvider.registerDataStore(name);
    }

    @Override
    public void registerDataStore(String prefix, int partitions) {
        if(runAsMirror) {
            this.dataStoreProvider.create(prefix, serviceContext.node().partitionNumber());
            return;
        }
        BackupProvider backupProvider = bMap.get(_type(prefix));
        if(backupProvider == null) return;
        backupProvider.registerDataStore(prefix,partitions);
    }

    @Override
    public void configure(Map<String, Object> properties) {

    }

    @Override
    public <T extends Recoverable> void update(Metadata metadata, String key, T t) {
        if(runAsMirror) return;
        BackupProvider backupProvider = bMap.get(metadata.typeId());
        if(backupProvider == null) return;
        backupProvider.update(metadata,key,t);
    }

    @Override
    public <T extends Recoverable> void create(Metadata metadata, String key, T t) {
        if(runAsMirror) return;
        BackupProvider backupProvider = bMap.get(metadata.typeId());
        if(backupProvider == null) return;
        backupProvider.create(metadata,key,t);
    }

    @Override
    public void update(Metadata metadata, String key, byte[] t) {
        if(!runAsMirror) return;
        if(metadata.scope() == Distributable.DATA_SCOPE){
            byte[] r = RevisionObject.toBinary(metadata.revision(),t,true);
            this.dataStoreProvider.create(metadata.source(),serviceContext.node().partitionNumber()).backup().set(key.getBytes(),r);
            return;
        }
        if(metadata.scope() == Distributable.INTEGRATION_SCOPE){
            this.dataStoreProvider.create(metadata.source()).backup().set(key.getBytes(),t);
            return;
        }
    }

    @Override
    public  void create(Metadata metadata, String key, byte[] t) {
        if(!runAsMirror) return;
        if(metadata.scope() == Distributable.DATA_SCOPE){
            byte[] r = RevisionObject.toBinary(metadata.revision(),t,true);
            this.dataStoreProvider.create(metadata.source(),serviceContext.node().partitionNumber()).backup().set(key.getBytes(),r);
            return;
        }
        if(metadata.scope() == Distributable.INTEGRATION_SCOPE){
            this.dataStoreProvider.create(metadata.source()).backup().set(key.getBytes(),t);
            return;
        }
    }
    public <T extends Recoverable> void backup(OnReplication[] onReplications,int size){

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
        log.warn("Run mirror backup provider ["+runAsMirror+"]");
    }

    public void addBackupProvider(BackupProvider backupProvider){
        bMap.put(backupProvider.name(),backupProvider);
    }
    public void removeBackupProvider(BackupProvider backupProvider){
        bMap.remove(backupProvider.name());
    }

    private String _type(String source){
        int ix = source.indexOf("_");
        if(ix<=0){
            return source;
        }
        else{
            return source.substring(0,ix);
        }
    }
}
