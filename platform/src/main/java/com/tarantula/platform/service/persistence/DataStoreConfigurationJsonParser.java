package com.tarantula.platform.service.persistence;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Configuration;
import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.Serviceable;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.service.DataStoreProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class DataStoreConfigurationJsonParser implements Serviceable {

    private String dataBucketGroup;
    private String dataBucketNode;

    private String dataStoreProviderConfiguration;
    private String dataDir;

    private int partitionNumber;

    private int maxReplicationNumber;

    private boolean dataStoreDailyBackup;


    private ServiceContext serviceContext;
    private DataStoreProvider.OnStart onStart;
    private ClusterProvider.Node node;

    private int snowflakeNodeNumber;
    private long snowflakeEpochStart;

    public DataStoreConfigurationJsonParser(String dconfig,ServiceContext tx,int maxReplicationNumber, DataStoreProvider.OnStart onStart){
        this.dataStoreProviderConfiguration = dconfig;
        this.node = tx.node();
        this.dataBucketGroup = node.bucketName();
        this.dataBucketNode = node.nodeName();
        this.partitionNumber = node.partitionNumber();//tx.platformRoutingNumber;
        this.dataDir = node.dataStoreDirectory();//tx.dataStoreDir;
        this.dataStoreDailyBackup = node.dailyBackupEnabled();//tx.dataStoreDailyBackup;
        this.serviceContext = tx;
        this.onStart = onStart;
    }
    private void parse(InputStream json){
        HashMap<String,Object> properties = new HashMap();
        JsonObject config = JsonUtil.parse(json);
        JsonObject ds = config.get("data-source").getAsJsonObject();
        String name = (ds.get("name").getAsString());
        if(!name.equals(DeploymentServiceProvider.DEPLOY_DATA_STORE)) throw new RuntimeException("master data store name must be->"+DeploymentServiceProvider.DEPLOY_DATA_STORE);
        String provider = ds.get("provider").getAsString();
        properties.put("name",name.trim());
        properties.put("bucket",this.dataBucketGroup);
        properties.put("node",this.dataBucketNode);
        properties.put("partitionNumber",this.partitionNumber);
        properties.put("maxReplicationNumber",this.maxReplicationNumber);
        properties.put("dir",this.dataDir);
        properties.put("dailyBackup",dataStoreDailyBackup);
        properties.put("node",this.node);
        DataStoreProvider dataStoreProvider = dataStoreProvider(provider.trim());
        JsonArray props = ds.get("properties").getAsJsonArray();
        props.forEach(e->{
            JsonObject kv = e.getAsJsonObject();
            kv.entrySet().forEach((v)->{
                properties.put(v.getKey(),v.getValue());
            });
        });

        Map<String,Object> _intrgration = new HashMap<>();
        JsonObject _iconfig = config.get("integration-backup-router").getAsJsonObject();
        _intrgration.put("enabled",node.runAsMirror()? false : node.backupEnabled());
        JsonArray ilist = _iconfig.get("backup-provider-list").getAsJsonArray();
        Configuration exconfig = this.serviceContext.configuration("tarantula-backup-router");
        for(JsonElement je : ilist) {
            JsonObject p = je.getAsJsonObject();
            if(p.get("enabled").getAsBoolean()){
                String _n = p.get("name").getAsString();
                if(exconfig==null) break;
                Object ref = exconfig.property(_n);
                if(ref==null){
                    _intrgration.put("enabled",false);
                }
                else{
                    JsonArray pts = ((JsonElement)ref).getAsJsonArray();
                    p.add("properties",pts);
                }
                _intrgration.put("backup-provider",p);
                break;
            }
        }
        Map<String,Object> _data = new HashMap<>();
        JsonObject _dconfig = config.get("data-backup-router").getAsJsonObject();
        _data.put("enabled",node.runAsMirror()? false : node.backupEnabled());
        JsonArray dlist = _dconfig.get("backup-provider-list").getAsJsonArray();
        for(JsonElement je : dlist) {
            JsonObject p = je.getAsJsonObject();
            if(p.get("enabled").getAsBoolean()){
                String _n = p.get("name").getAsString();
                if(exconfig==null) break;
                Object ref = exconfig.property(_n);
                if(ref==null){
                    _data.put("enabled",false);
                }
                else{
                    JsonArray pts = ((JsonElement)ref).getAsJsonArray();
                    p.add("properties",pts);
                }
                _data.put("backup-provider",p);
                break;
            }
        }
        properties.put("integrationRouter",_intrgration);
        properties.put("dataRouter",_data);
        properties.put("serviceContext",this.serviceContext);
        dataStoreProvider.configure(properties);
        onStart.on(dataStoreProvider);
    }


    DataStoreProvider dataStoreProvider(String provider){
        try {
            return (DataStoreProvider)Class.forName(provider).getConstructor().newInstance();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }


    public void start() throws Exception {
        try{
            File f = new File("/etc/tarantula/"+this.dataStoreProviderConfiguration);
            InputStream in = new FileInputStream(f);
            this.parse(in);
        }catch (Exception ex){
            this.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream(this.dataStoreProviderConfiguration));
        }
    }


    public void shutdown() throws Exception {

    }
}
