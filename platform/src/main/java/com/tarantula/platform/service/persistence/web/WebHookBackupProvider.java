package com.tarantula.platform.service.persistence.web;

import com.icodesoftware.Recoverable;
import com.icodesoftware.service.BackupProvider;
import com.icodesoftware.service.Metadata;
import com.icodesoftware.util.HttpCaller;
import com.tarantula.platform.configuration.WebHookConfiguration;

import java.util.Map;

public class WebHookBackupProvider extends HttpCaller implements BackupProvider {


    private WebHookConfiguration webHookConfiguration;

    public WebHookBackupProvider(){
    }

    public WebHookBackupProvider(WebHookConfiguration webHookConfiguration){
        this.webHookConfiguration = webHookConfiguration;
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
