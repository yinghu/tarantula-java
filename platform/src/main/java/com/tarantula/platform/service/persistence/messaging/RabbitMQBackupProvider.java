package com.tarantula.platform.service.persistence.messaging;

import com.icodesoftware.Recoverable;
import com.icodesoftware.service.BackupProvider;
import com.icodesoftware.service.Metadata;
import com.icodesoftware.service.OnReplication;

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
    public void registerDataStore(int scope,String name) {

    }



    @Override
    public void configure(Map<String, Object> properties) {

    }


    public void batch(OnReplication[] onReplications,int size){

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
