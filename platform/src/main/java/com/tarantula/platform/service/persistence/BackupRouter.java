package com.tarantula.platform.service.persistence;

import com.google.gson.JsonObject;
import com.icodesoftware.Recoverable;

import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.BackupProvider;
import com.icodesoftware.service.OnReplication;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.TokenValidatorProvider;

import java.util.HashMap;
import java.util.Map;

public class BackupRouter implements BackupProvider {

    private JDKLogger logger = JDKLogger.getLogger(BackupRouter.class);

    private final String name;
    private final int scope;

    private boolean enabled;


    private BackupProvider router;

    public BackupRouter(String name,int scope){
        this.name = name;
        this.scope = scope;
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

    public void configure(Map<String,Object> properties){
        this.enabled = (Boolean)properties.get("enabled");
        if(!this.enabled) {
            logger.warn("Backup router enabled ["+name+"/"+this.enabled+"]");
            return;
        }
        JsonObject provider = (JsonObject)properties.get("backup-provider");
        String bname = provider.get("name").getAsString();
        String cname = provider.get("provider").getAsString();
        try{
            BackupProvider systemBackupProvider = (BackupProvider)Class.forName(cname.trim()).getConstructor().newInstance();
            systemBackupProvider.enabled(this.enabled);
            Map<String,Object> props = new HashMap<>();
            provider.get("properties").getAsJsonArray().forEach((e)->{
                JsonObject kv = e.getAsJsonObject();
                kv.entrySet().forEach((v)->{
                    props.put(v.getKey(),v.getValue());
                });
            });
            props.put("scope",scope);
            systemBackupProvider.configure(props);
            //systemBackupProvider.setup();
            this.router = systemBackupProvider;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
        logger.warn("Backup router ["+name+"/"+bname+"] enabled ["+enabled+"]");
    }


    @Override
    public void registerDataStore(String storeName) {
        if(!this.enabled) return;
        router.registerDataStore(storeName);
    }

    @Override
    public void registerDataStore(String storeNamePrefix, int partition) {
        if(!this.enabled) return;
        router.registerDataStore(storeNamePrefix,partition);
    }

    public void batch(OnReplication[] onReplications,int size){
        if(!this.enabled) return;
        router.batch(onReplications,size);
    }
    public void setup(ServiceContext tcx){
        if(router==null) return;
        router.setup(tcx);
    }

}
