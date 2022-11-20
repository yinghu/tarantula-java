package com.tarantula.platform.service.persistence.web;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.BackupProvider;
import com.icodesoftware.service.OnReplication;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.configuration.WebHookConfiguration;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.util.Map;

public class WebHookBackupProvider implements BackupProvider {

    private static JDKLogger log = JDKLogger.getLogger(WebHookBackupProvider.class);

    private WebHookConfiguration webHookConfiguration;
    private String accessKey;
    private String path;
    private String host;
    private boolean enabled;
    private int scope;
    private ServiceContext serviceContext;
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
                    Session.TARANTULA_ACTION,"onDataStore",
                    Session.TARANTULA_ACCESS_KEY,accessKey,
            };
            JsonObject config = new JsonObject();
            config.addProperty("scope",Distributable.INTEGRATION_SCOPE);
            config.addProperty("name",name);
            serviceContext.httpClientProvider().post(host,path,headers,config.toString().getBytes());
        }catch (Exception ex){
           log.error("error on register data store",ex);
        }
    }

    @Override
    public void registerDataStore(String prefix, int partitions) {
        try {
            String[] headers = new String[]{
                    Session.TARANTULA_ACTION,"onDataStore",
                    Session.TARANTULA_ACCESS_KEY,accessKey,
            };
            JsonObject config = new JsonObject();
            config.addProperty("scope",Distributable.DATA_SCOPE);
            config.addProperty("name",prefix);
            config.addProperty("partition",partitions);
            serviceContext.httpClientProvider().post(host,path,headers,config.toString().getBytes());
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
    }


    public void batch(OnReplication[] onReplications,int size){
        try {
            String[] headers = new String[]{
                    Session.TARANTULA_ACTION,"onBatch",
                    Session.TARANTULA_ACCESS_KEY,accessKey,
            };
            JsonObject payload = new JsonObject();
            payload.addProperty("scope",scope);
            JsonArray updates = new JsonArray();
            for(int i=0;i<size;i++){
                Recoverable ref = onReplications[i].recoverable();
                JsonObject update = new JsonObject();
                update.addProperty("key",onReplications[i].keyAsString());
                update.addProperty("factoryId",ref.getFactoryId());
                update.addProperty("classId",ref.getClassId());
                update.addProperty("revision",ref.revision());
                update.add("payload",JsonUtil.toJsonObject(ref.toMap()));
                updates.add(update);
                //if(ref.getFactoryId()== PresencePortableRegistry.OID && ref.getClassId() == PresencePortableRegistry.PRESENCE_CID){
                    //log.warn(update.toString());
                    //log.warn(new String(ref.toBinary()));
                    //log.warn(JsonUtil.toJsonString(ref.toMap()));
                    //RecoverableRegistry registry = serviceContext.recoverableRegistry(PresencePortableRegistry.OID);
                    //Recoverable t = registry.create(ref.getClassId());
                    //t.fromBinary(update.get("payload").getAsJsonObject().toString().getBytes());
                    //log.warn(JsonUtil.toJsonString(t.toMap()));
                //}
            }
            payload.add("updates",updates);
            String resp = serviceContext.httpClientProvider().post(host,path,headers,payload.toString().getBytes());
        }catch (Exception ex){
            log.error("error on back",ex);
        }
    }
    public void setup(ServiceContext tcx){
        this.serviceContext = tcx;
        log.warn("Web hook backup provider started");
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
    }

    @Override
    public void shutdown() throws Exception {

    }
}
