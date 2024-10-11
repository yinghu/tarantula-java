package com.icodesoftware.lmdb;

import com.google.gson.JsonObject;
import com.icodesoftware.Distributable;

public class EnvSetting {

    public static final String ENV_CONFIG_NAME = "name";
    public static final String ENV_CONFIG_BASE_DIR = "dir";
    public static final String ENV_CONFIG_STORE_SIZE_MB = "storeSizeMb";
    public static final String ENV_CONFIG_NO_SYNC_FLAG = "envNoSyncFlag";
    public static final String ENV_CONFIG_STORE_REINDEXING = "storeReindexing";
    public static final String ENV_CONFIG_EXTERNAL_KEY_VALUE_BUFFER_USED = "externalKeyValueBufferUsed";
    public static final String ENV_CONFIG_STORE_KEY_SIZE= "storeKeySize";
    public static final String ENV_CONFIG_STORE_VALUE_SIZE = "storeValueSize";
    public static final String ENV_CONFIG_STORE_PENDING_BUFFER_SIZE = "storePendingBufferSize";
    public static final String ENV_CONFIG_MIGRATION = "migration";


    public static final long MB_1 = 1_048_576L;

    public static final String ENV_PROVIDER_NAME ="tarantula";
    public static final String ENV_BASE_DIR ="target/lmdb";
    public static final boolean ENV_NO_SYNC_Flag = true;
    public static final int MAX_LMDB_KEY_SIZE = 511;
    public static final int MAX_STORE_NUMBER = 1024;
    public static final int MAX_READER_NUMBER = 100;
    public static final int KEY_SIZE = 200;//less than 511
    //NOTES : key+value (4096-16)/2-8< 2032 bytes ( 200 bytes for key ; value <= 1832 bytes (2032 - 200)
    public static final int VALUE_SIZE = 1832; //less than 1832
    public static final int MAX_PENDING_BUFFER_NUMBER = 32;

    public static final String data ="data";
    public static final String integration ="integration";
    public static final String index ="index";
    public static final String log ="log";
    public static final String local ="local";

    public static final EnvSetting DataSetting = new EnvSetting(data,ENV_BASE_DIR+"/data",0,true);
    public static final EnvSetting IntegrationSetting = new EnvSetting(integration,ENV_BASE_DIR+"/integration",0,true);
    public static final EnvSetting IndexSetting = new EnvSetting(index,ENV_BASE_DIR+"/index",0,true);
    public static final EnvSetting LogSetting = new EnvSetting(log,ENV_BASE_DIR+"/log",0,true);
    public static final EnvSetting LocalSetting = new EnvSetting(local,ENV_BASE_DIR+"/local",0,true);

    public EnvSetting(String name,String storePath,int mbSize,boolean enabled){
        this.name = name;
        this.storePath = storePath;
        this.mbSize = mbSize;
        this.enabled = enabled;
        this.scope = scope();
    }
    public int scope;
    public String name;
    public String storePath;
    public int mbSize;
    public boolean enabled;

    private int scope(){
        if(name.equals(data)) return Distributable.DATA_SCOPE;
        if(name.equals(integration)) return Distributable.INTEGRATION_SCOPE;
        if(name.equals(index)) return Distributable.INDEX_SCOPE;
        if(name.equals(log)) return Distributable.LOG_SCOPE;
        if(name.equals(local)) return Distributable.LOCAL_SCOPE;
        throw new UnsupportedOperationException("named LMDB env ["+name+"] not supported");
    }

    public static long toBytesFromMb(int mbSize){
        return MB_1*mbSize;
    }

    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name",name);
        jsonObject.addProperty("scope",scope);
        jsonObject.addProperty("storePath",storePath);
        jsonObject.addProperty("mdSize",mbSize);
        jsonObject.addProperty("enabled",enabled);
        return jsonObject;
    }
}
