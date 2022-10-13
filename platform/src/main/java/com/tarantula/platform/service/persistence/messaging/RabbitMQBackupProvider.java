package com.tarantula.platform.service.persistence.messaging;

import com.icodesoftware.Recoverable;
import com.icodesoftware.service.BackupProvider;
import com.icodesoftware.service.Metadata;

import java.util.Map;

public class RabbitMQBackupProvider implements BackupProvider {

    private boolean enabled;


    @Override
    public boolean enabled() {
        return this.enabled;
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

    }

    @Override
    public void registerDataStore(String prefix, int partitions) {

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
    public String name() {
        return null;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
}
