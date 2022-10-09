package com.tarantula.platform.service.persistence;

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
    private CopyOnWriteArraySet<String> pendingSource = new CopyOnWriteArraySet<>();

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
    public int scope() {
        return scope;
    }

    @Override
    public void configure(Map<String, String> properties) {

    }

    @Override
    public void setup(ServiceContext serviceContext) {
        this.logger = serviceContext.logger(BackupRouter.class);
    }

    @Override
    public void registerDataStore(String name) {
        pendingSource.add(name);
    }

    @Override
    public void registerDataStore(String prefix, int partitions) {
        pendingSource.add(name);
    }


    @Override
    public <T extends Recoverable> void update(Metadata metadata, String key, T t) {

    }

    @Override
    public <T extends Recoverable> void create(Metadata metadata, String key, T t) {

    }

    public void addBackupProvider(BackupProvider backupProvider){
        logger.warn(backupProvider.name()+"/"+backupProvider.scope()+"/ registered on ["+name+"]");
        bMap.put(backupProvider.name(),backupProvider);
        pendingSource.forEach((src)->{
            //if(src.startsWith(backupProvider.name())){
                logger.warn(src);
            //}
        });
    }
    public void removeBackupProvider(BackupProvider backupProvider){
        logger.warn(backupProvider.name()+"/"+backupProvider.scope()+"/ unregistered on ["+name+"]");
        bMap.remove(backupProvider.name());
    }
}
