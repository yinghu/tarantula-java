package com.tarantula.platform.service.persistence;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.lmdb.EnvSetting;
import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.Serviceable;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.service.DataStoreProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;

public class DataStoreConfigurationJsonParser implements Serviceable {

    private String dataBucketGroup;
    private String dataBucketNode;

    private String dataStoreProviderConfiguration;
    private String dataDir;

    //private int partitionNumber;

    private int storeSizeMb;
    private int storeKeySize;
    private int storeValueSize;
    private int storePendingBufferSize;
    private boolean externalKeyValueBufferUsed;
    private boolean envNoSyncFlag;

    private boolean storeReindexing;
    private boolean dataStoreDailyBackup;

    private ServiceContext serviceContext;
    private DataStoreProvider.OnStart onStart;
    private ClusterProvider.Node node;



    public DataStoreConfigurationJsonParser(String config,ServiceContext tx,Map<String,Object> additions, DataStoreProvider.OnStart onStart){
        this.storeSizeMb = (int)additions.get(EnvSetting.ENV_CONFIG_STORE_SIZE_MB);
        this.envNoSyncFlag = (boolean)additions.get(EnvSetting.ENV_CONFIG_NO_SYNC_FLAG);
        this.storeReindexing = (boolean)additions.get(EnvSetting.ENV_CONFIG_STORE_REINDEXING);
        this.storeKeySize = (int)additions.get(EnvSetting.ENV_CONFIG_STORE_KEY_SIZE);
        this.storeValueSize = (int)additions.get(EnvSetting.ENV_CONFIG_STORE_VALUE_SIZE);
        this.storePendingBufferSize = (int)additions.get(EnvSetting.ENV_CONFIG_STORE_PENDING_BUFFER_SIZE);
        this.externalKeyValueBufferUsed = (boolean)additions.get(EnvSetting.ENV_CONFIG_EXTERNAL_KEY_VALUE_BUFFER_USED);
        this.dataStoreProviderConfiguration = config;
        this.node = tx.node();
        this.dataBucketGroup = node.bucketName();
        this.dataBucketNode = node.nodeName();
        //this.partitionNumber = node.bucketNumber();//tx.platformRoutingNumber;
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
        //properties.put("partitionNumber",this.partitionNumber);
        properties.put(EnvSetting.ENV_CONFIG_STORE_SIZE_MB,this.storeSizeMb);
        properties.put(EnvSetting.ENV_CONFIG_NO_SYNC_FLAG,envNoSyncFlag);
        properties.put(EnvSetting.ENV_CONFIG_STORE_REINDEXING,storeReindexing);
        properties.put(EnvSetting.ENV_CONFIG_STORE_KEY_SIZE,storeKeySize);
        properties.put(EnvSetting.ENV_CONFIG_STORE_VALUE_SIZE,storeValueSize);
        properties.put(EnvSetting.ENV_CONFIG_STORE_PENDING_BUFFER_SIZE,storePendingBufferSize);
        properties.put(EnvSetting.ENV_CONFIG_EXTERNAL_KEY_VALUE_BUFFER_USED,externalKeyValueBufferUsed);
        properties.put(EnvSetting.ENV_CONFIG_BASE_DIR,this.dataDir);
        properties.put("dailyBackup",dataStoreDailyBackup);
        properties.put("node",this.node);
        properties.put(EnvSetting.ENV_CONFIG_MIGRATION,ds.get("migration").getAsJsonObject());
        DataStoreProvider dataStoreProvider = dataStoreProvider(provider.trim());

        JsonArray envs = ds.get("env-settings").getAsJsonArray();
        envs.forEach(je->{
            JsonObject env = je.getAsJsonObject();
            String n = env.get("name").getAsString();
            String p = node.dataStoreDirectory()+ FileSystems.getDefault().getSeparator() + env.get("path").getAsString();
            int mz = env.get("mbSize").getAsInt();
            boolean e = env.get("enabled").getAsBoolean();
            properties.put(n,new EnvSetting(n,p,mz,e));
        });
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

    public static String storeIndexDir(String config){
        JsonObject json;
        try{
            File f = new File("/etc/tarantula/"+config);
            InputStream in = new FileInputStream(f);
            json = JsonUtil.parse(in);
        }catch (Exception ex){
            json = JsonUtil.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream(config));
        }
        JsonArray props = json.get("data-source").getAsJsonObject().getAsJsonArray("env-settings");
        String[] indexDir = {"tarantula/index"};
        props.forEach(p->{
            JsonObject pd = p.getAsJsonObject();
            if(pd.get("name").equals(EnvSetting.index)){
                indexDir[0] = pd.get("path").getAsString();
            }
        });
        return indexDir[0];
    }
}
