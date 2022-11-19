package com.tarantula.platform.service.persistence.web;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Distributable;
import com.icodesoftware.OnAccess;
import com.icodesoftware.Recoverable;
import com.icodesoftware.Session;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.BackupProvider;
import com.icodesoftware.service.OnReplication;
import com.icodesoftware.util.HttpCaller;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.configuration.WebHookConfiguration;


import java.awt.*;
import java.util.Map;

public class WebHookBackupProvider extends HttpCaller implements BackupProvider {

    private static JDKLogger log = JDKLogger.getLogger(WebHookBackupProvider.class);

    private WebHookConfiguration webHookConfiguration;
    private String accessKey;
    private String path;

    private boolean enabled;
    private int scope;
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
        return scope;
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
           log.error("error on register data store",ex);
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
            log.error("error on register data store",ex);
        }
    }

    @Override
    public void configure(Map<String, Object> properties) {
        this.host = ((JsonElement) properties.get("host")).getAsString();
        this.accessKey = ((JsonElement) properties.get("accessKey")).getAsString();
        this.path = ((JsonElement) properties.get("path")).getAsString();
        this.scope = (Integer)properties.get("scope");
        try{
            super._init();
        }catch (Exception ex){
            log.error("configure",ex);
            throw new RuntimeException(ex);
        }
    }


    public void batch(OnReplication[] onReplications,int size){
        try {
            String[] headers = new String[]{
                    Session.TARANTULA_ACTION,"onBatch",
                    Session.TARANTULA_ACCESS_KEY,accessKey,
                    Session.TARANTULA_NAME,""+scope
            };
            JsonObject payload = new JsonObject();
            JsonArray updates = new JsonArray();
            for(int i=0;i<size;i++){
                Recoverable ref = onReplications[i].recoverable();
                JsonObject update = new JsonObject();
                update.addProperty("key",onReplications[i].keyAsString());
                update.addProperty("factoryId",ref.getFactoryId());
                update.addProperty("classId",ref.getClassId());
                update.addProperty("revision",ref.revision());
                //update.add("payload",);
                updates.add(update);
            }
            payload.add("updates",updates);
            String resp = super.post(path, payload.toString().getBytes(),headers);
        }catch (Exception ex){
            log.error("error on back",ex);
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
