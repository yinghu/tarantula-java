package com.tarantula.platform.service.persistence;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Configuration;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.service.Serviceable;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.service.DataStoreProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class DataStoreConfigurationJsonParser implements Serviceable {


    HashMap<String,Object> properties = new HashMap();

    private String dataBucketGroup;
    private String dataBucketNode;

    private String dataStoreProviderConfiguration;
    private String dataDir;

    private int partitionNumber;
    private int accessIndexPartitionNumber;
    private boolean dataStoreDailyBackup;


    private TarantulaContext tarantulaContext;

    public DataStoreConfigurationJsonParser(String dconfig, TarantulaContext tx){
        this.dataStoreProviderConfiguration = dconfig;
        this.dataBucketGroup = tx.dataBucketGroup;
        this.dataBucketNode = tx.dataBucketNode;
        this.partitionNumber = tx.platformRoutingNumber;
        this.accessIndexPartitionNumber = tx.accessIndexRoutingNumber;
        this.dataDir = tx.dataStoreDir;
        this.dataStoreDailyBackup = tx.dataStoreDailyBackup;
        this.tarantulaContext = tx;
    }
    private void parse(InputStream json) throws Exception{
        JsonObject config = JsonUtil.parse(json);
        JsonObject ds = config.get("data-source").getAsJsonObject();
        String name = (ds.get("name").getAsString());
        if(!name.equals(DeploymentServiceProvider.DEPLOY_DATA_STORE)) throw new RuntimeException("master data store name must be->"+DeploymentServiceProvider.DEPLOY_DATA_STORE);
        String provider = ds.get("provider").getAsString();
        properties.put("name",name.trim());
        properties.put("bucket",this.dataBucketGroup);
        properties.put("node",this.dataBucketNode);
        properties.put("partitionNumber",this.partitionNumber);
        properties.put("dir",this.dataDir);
        properties.put("poolSetting",this.tarantulaContext.dataReplicationThreadPoolSetting);
        properties.put("dailyBackup",dataStoreDailyBackup);

        this.tarantulaContext.deploymentDataStoreProvider = dataStoreProvider(provider.trim());
        JsonArray props = ds.get("properties").getAsJsonArray();
        props.forEach(e->{
            JsonObject kv = e.getAsJsonObject();
            kv.entrySet().forEach((v)->{
                properties.put(v.getKey(),v.getValue());
            });
        });

        Map<String,Object> _intrgration = new HashMap<>();
        JsonObject _iconfig = config.get("integration-backup-router").getAsJsonObject();
        _intrgration.put("enabled",tarantulaContext.runAsMirror? false : tarantulaContext.backupEnabled);
        JsonArray ilist = _iconfig.get("backup-provider-list").getAsJsonArray();
        Configuration exconfig = this.tarantulaContext.configuration("tarantula-backup-router");
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
        _data.put("enabled",tarantulaContext.runAsMirror? false : tarantulaContext.backupEnabled);
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
        boolean backupEnabled = (boolean)_intrgration.get("enabled") || (boolean)_data.get("enabled");
        properties.put("backupEnabled",backupEnabled);
        this.tarantulaContext.deploymentDataStoreProvider.configure(properties);
        this.tarantulaContext.deploymentDataStoreProvider.start();
        this.tarantulaContext.deploymentDataStoreProvider.setup(tarantulaContext);
        this.tarantulaContext._initMirrorClusterBackup();
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
