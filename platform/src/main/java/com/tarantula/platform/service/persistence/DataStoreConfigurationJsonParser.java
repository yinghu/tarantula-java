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

    private int storeSizeMb;

    private boolean envNoSyncFlag;

    private boolean dataStoreDailyBackup;


    private ServiceContext serviceContext;
    private DataStoreProvider.OnStart onStart;
    private ClusterProvider.Node node;



    public DataStoreConfigurationJsonParser(String config,ServiceContext tx,Map<String,Object> additions, DataStoreProvider.OnStart onStart){
        this.storeSizeMb = (int)additions.get("storeSizeMb");
        this.envNoSyncFlag = (boolean)additions.get("envNoSyncFlag");
        this.dataStoreProviderConfiguration = config;
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
        properties.put("storeSizeMb",this.storeSizeMb);
        properties.put("envNoSyncFlag",envNoSyncFlag);
        properties.put("dir",this.dataDir);
        properties.put("dailyBackup",dataStoreDailyBackup);
        properties.put("node",this.node);
        properties.put("migrationListeners",ds.get("migrationListeners").getAsJsonArray());
        DataStoreProvider dataStoreProvider = dataStoreProvider(provider.trim());
        JsonArray props = ds.get("properties").getAsJsonArray();
        props.forEach(e->{
            JsonObject kv = e.getAsJsonObject();
            kv.entrySet().forEach((v)->{
                properties.put(v.getKey(),v.getValue());
            });
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
        JsonArray props = json.get("data-source").getAsJsonObject().getAsJsonArray("properties");
        String[] indexDir = {"tarantula/index"};
        props.forEach(p->{
            JsonObject pd = p.getAsJsonObject();
            if(pd.has("indexPath")){
                indexDir[0] = pd.get("indexPath").getAsString();
            }
        });
        return indexDir[0];
    }
}
