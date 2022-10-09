package com.tarantula.platform.service.persistence;

import com.icodesoftware.Recoverable;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.BackupProvider;
import com.icodesoftware.service.Metadata;
import com.icodesoftware.service.ServiceContext;

import java.util.Map;

public class BackupRouter implements BackupProvider {

    private static JDKLogger log = JDKLogger.getLogger(BackupRouter.class);

    private final String name;
    private final int scope;

    private ServiceContext serviceContext;

    private boolean enabled;


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
        this.serviceContext = serviceContext;
    }

    @Override
    public void registerDataStore(String name) {

    }

    @Override
    public void registerDataStore(String prefix, int partitions) {

    }



    @Override
    public <T extends Recoverable> void update(Metadata metadata, String key, T t) {


    }

    @Override
    public <T extends Recoverable> void create(Metadata metadata, String key, T t) {

    }

    public void addBackupProvider(BackupProvider backupProvider){
        log.warn(backupProvider.name()+"/"+backupProvider.scope()+"/ registered on ["+name+"]");
    }
    public void removeBackupProvider(BackupProvider backupProvider){
        log.warn(backupProvider.name()+"/"+backupProvider.scope()+"/ unregistered on ["+name+"]");
    }
}
