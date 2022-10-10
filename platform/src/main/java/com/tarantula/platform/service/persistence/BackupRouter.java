package com.tarantula.platform.service.persistence;

import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.BackupProvider;
import com.icodesoftware.service.Metadata;
import com.icodesoftware.service.ServiceContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class BackupRouter implements BackupProvider {

    private TarantulaLogger logger;

    private final String name;
    private final int scope;

    private boolean enabled;

    private ConcurrentHashMap<String,BackupProvider> bMap = new ConcurrentHashMap();
    private CopyOnWriteArraySet<BackupSource> pendingSource = new CopyOnWriteArraySet<>();

    public BackupRouter(String name,int scope,boolean enabled){
        this.name = name;
        this.scope = scope;
        this.enabled = enabled;
    }
    @Override
    public String name() {
        return name;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }

    @Override
    public boolean enabled() {
        return this.enabled;
    }

    @Override
    public void enabled(boolean enabled){

    }

    @Override
    public int scope() {
        return scope;
    }



    @Override
    public void setup(ServiceContext serviceContext) {
        this.logger = serviceContext.logger(BackupRouter.class);
    }

    @Override
    public void registerDataStore(String storeName) {
        pendingSource.add(new BackupSource(storeName,Distributable.INTEGRATION_SCOPE,0));
    }

    @Override
    public void registerDataStore(String storeNamePrefix, int partition) {
        pendingSource.add(new BackupSource(storeNamePrefix,Distributable.DATA_SCOPE,partition));
    }


    @Override
    public <T extends Recoverable> void update(Metadata metadata, String key, T t) {
        if(!enabled) return;
    }

    @Override
    public <T extends Recoverable> void create(Metadata metadata, String key, T t) {
        if(!enabled) return;
    }

    public void addBackupProvider(BackupProvider backupProvider){
        logger.warn(backupProvider.name()+"/"+backupProvider.scope()+"/ registered on ["+name+"]");
        backupProvider.enabled(this.enabled);
        try{
            backupProvider.start();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
        bMap.put(backupProvider.name(),backupProvider);
        pendingSource.forEach((src)->{
            if(src.name.startsWith(backupProvider.name())){
                if(backupProvider.scope() == Distributable.DATA_SCOPE){
                    backupProvider.registerDataStore(src.name,src.partition);
                }
                else if(backupProvider.scope() == Distributable.INTEGRATION_SCOPE){
                    backupProvider.registerDataStore(src.name);
                }
            }
        });
    }
    public void removeBackupProvider(BackupProvider backupProvider){
        logger.warn(backupProvider.name()+"/"+backupProvider.scope()+"/ unregistered on ["+name+"]");
        bMap.remove(backupProvider.name());
    }
}
