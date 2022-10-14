package com.tarantula.platform.service.persistence.web;

import com.google.gson.JsonElement;
import com.icodesoftware.Distributable;
import com.icodesoftware.OnAccess;
import com.icodesoftware.Recoverable;
import com.icodesoftware.Session;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.BackupProvider;
import com.icodesoftware.service.Metadata;
import com.icodesoftware.util.HttpCaller;
import com.tarantula.platform.configuration.WebHookConfiguration;
import com.tarantula.platform.service.persistence.RecoverableMetadata;


import java.util.Map;

public class WebHookBackupProvider extends HttpCaller implements BackupProvider {

    //private static JDKLogger log = JDKLogger.getLogger(WebHookBackupProvider.class);

    private WebHookConfiguration webHookConfiguration;
    private String accessKey;
    private String path;

    private boolean enabled;
    public WebHookBackupProvider(){
    }

    public WebHookBackupProvider(WebHookConfiguration webHookConfiguration){
        this.webHookConfiguration = webHookConfiguration;
    }


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
        try {
            String[] headers = new String[]{
                    Session.TARANTULA_ACTION,"onRegister",
                    Session.TARANTULA_ACCESS_KEY,accessKey,
                    Session.TARANTULA_NAME, Distributable.INTEGRATION_SCOPE+"#"+name+"#0",
            };
            super.get(path,headers);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void registerDataStore(String prefix, int partitions) {
        try {
            String[] headers = new String[]{
                    Session.TARANTULA_ACTION,"onRegister",
                    Session.TARANTULA_ACCESS_KEY,accessKey,
                    Session.TARANTULA_NAME,Distributable.DATA_SCOPE+"#"+prefix+"#"+partitions,
            };
            super.get(path,headers);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void configure(Map<String, Object> properties) {
        this.host = ((JsonElement) properties.get("host")).getAsString();
        this.accessKey = ((JsonElement) properties.get("accessKey")).getAsString();
        this.path = ((JsonElement) properties.get("path")).getAsString();
        try{super._init();}catch (Exception ex){ex.printStackTrace();}
    }

    @Override
    public <T extends Recoverable> void update(Metadata metadata, String key, T t) {
        try {
            String[] headers = new String[]{
                    Session.TARANTULA_ACTION,"onUpdate",
                    Session.TARANTULA_ACCESS_KEY,accessKey,
                    Session.TARANTULA_NAME,key+"#"+metadata.toJson()
            };
            String resp = super.post(path,t.toBinary(),headers);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public <T extends Recoverable> void create(Metadata metadata, String key, T t) {
        try {
            String[] headers = new String[]{
                    Session.TARANTULA_ACTION,"onCreate",
                    Session.TARANTULA_ACCESS_KEY,accessKey,
                    Session.TARANTULA_NAME,key+"#"+metadata.toJson()
            };
            String resp = super.post(path,t.toBinary(),headers);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public String name() {
        return OnAccess.WEB_HOOK;
    }

    @Override
    public void start() throws Exception {
        this.host = webHookConfiguration.host();
        this.accessKey = webHookConfiguration.accessKey();
        this.path = webHookConfiguration.path();
        super._init();
    }

    @Override
    public void shutdown() throws Exception {

    }
}
