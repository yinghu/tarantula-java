package com.tarantula.platform.service.persistence;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableRegistry;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.BackupProvider;
import com.icodesoftware.service.OnReplication;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.service.DataStoreProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class MirrorClusterBackupProvider implements BackupProvider{

    private JDKLogger log = JDKLogger.getLogger(MirrorClusterBackupProvider.class);

    private DataStoreProvider dataStoreProvider;
    private boolean runAsMirror;
    private long nextSyncInterval;
    private MirrorBackupSynchronizer mirrorBackupSynchronizer;
    private ServiceContext serviceContext;
    private ConcurrentHashMap<String,BackupProvider> bMap;

    private ConcurrentLinkedDeque<OnReplication> pendingBatches = new ConcurrentLinkedDeque<>();

    public MirrorClusterBackupProvider(DataStoreProvider dataStoreProvider){
        this.dataStoreProvider = dataStoreProvider;
    }

    @Override
    public boolean enabled() {
        return runAsMirror;
    }

    @Override
    public void enabled(boolean enabled) {
        this.runAsMirror = enabled;
    }

    @Override
    public int scope() {
        return 0;
    }

    @Override
    public void registerDataStore(int scope,String name) {
        if(runAsMirror){
            if(scope==Distributable.DATA_SCOPE){
                this.dataStoreProvider.create(name,serviceContext.node().partitionNumber());
            }
            else if(scope==Distributable.INTEGRATION_SCOPE){
                this.dataStoreProvider.create(name);
            }
            return;
        }
        BackupProvider backupProvider = bMap.get(_type(name));
        if(backupProvider == null) return;
        backupProvider.registerDataStore(scope,name);
    }

    @Override
    public void configure(Map<String, Object> properties) {
        nextSyncInterval = ((Number)properties.get("syncIntervalSeconds")).intValue()*1000;
    }


    public void batch(OnReplication[] onReplications,int size){
        pendingBatches.offer(onReplications[0]);
    }
    public void _batch(){
        //log.warn("Running batch ..");
        try{
            OnReplication onReplication = pendingBatches.poll();
            if(onReplication!=null){
                JsonObject updates = JsonUtil.parse(onReplication.value());
                int scope = updates.get("scope").getAsInt();
                JsonArray batch = updates.getAsJsonArray("updates");
                batch.forEach(e->{
                    JsonObject data = e.getAsJsonObject();
                    //log.warn(data.toString());
                    String key = data.get("key").getAsString();
                    String source = data.get("source").getAsString();
                    //int factoryId = data.get("factoryId").getAsInt();
                    //int classId = data.get("classId").getAsInt();
                    long revision = data.get("revision").getAsLong();
                    JsonObject payload = data.get("payload").getAsJsonObject();
                    if(scope == Distributable.DATA_SCOPE){
                        DataStore ds = this.dataStoreProvider.create(source,serviceContext.node().partitionNumber());
                        ds.backup().set(key.getBytes(),RevisionObject.toBinary(revision,payload.toString().getBytes(),true));                    }
                    else if(scope == Distributable.INTEGRATION_SCOPE){
                        DataStore ds = this.dataStoreProvider.create(source);
                        ds.backup().set(key.getBytes(),payload.toString().getBytes());
                    }
                });
            }
        }
        catch (Exception ex){
            log.error("_batch",ex);
        }
        this.serviceContext.schedule(this.mirrorBackupSynchronizer);
    }
    @Override
    public String name() {
        return "MirrorClusterBackup";
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }

    public void setup(ServiceContext serviceContext){
        this.serviceContext = serviceContext;
        this.bMap = new ConcurrentHashMap<>();
        log.warn("Run mirror backup provider ["+runAsMirror+"]");
        if(runAsMirror){
            this.mirrorBackupSynchronizer = new MirrorBackupSynchronizer(this,nextSyncInterval);
            this.serviceContext.schedule(this.mirrorBackupSynchronizer);
        }
    }

    public void addBackupProvider(BackupProvider backupProvider){
        bMap.put(backupProvider.name(),backupProvider);
    }
    public void removeBackupProvider(BackupProvider backupProvider){
        bMap.remove(backupProvider.name());
    }

    private String _type(String source){
        int ix = source.indexOf("_");
        if(ix<=0){
            return source;
        }
        else{
            return source.substring(0,ix);
        }
    }
}
