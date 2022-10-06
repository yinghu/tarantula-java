package com.tarantula.platform.service.persistence;

import com.icodesoftware.Recoverable;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.Metadata;
import com.icodesoftware.service.ServiceContext;

import java.util.Map;

public class BackupRouter implements BackupProvider{

    private static JDKLogger log = JDKLogger.getLogger(BackupRouter.class);

    private String name;
    private int scope;

    private ServiceContext serviceContext;
    private boolean enabled;


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
        this.name = properties.get("name");
        this.scope = Integer.parseInt(properties.get("scope"));
        this.enabled = Boolean.parseBoolean(properties.get("enabled"));
        log.warn("Backup provider scope: ["+scope+"] name :["+name+"] enabled :["+enabled+"]");
        //if(!enabled) return;
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
    public <T extends Recoverable> T load(Metadata metadata, String key) {
        return null;
    }

    @Override
    public <T extends Recoverable> byte[] update(Metadata metadata, String key, T t) {
        return new byte[0];
    }

    @Override
    public <T extends Recoverable> byte[] create(Metadata metadata, String key, T t) {
        return new byte[0];
    }
}
