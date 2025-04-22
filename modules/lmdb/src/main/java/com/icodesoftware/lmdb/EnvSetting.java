package com.icodesoftware.lmdb;

import com.google.gson.JsonObject;
import com.icodesoftware.Distributable;
import com.icodesoftware.lmdb.ffm.NativeUtil;
import com.icodesoftware.util.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Path;

public class EnvSetting {

    public static final long REVISION_START = 1L;
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

    public final int scope;
    public final String name;
    public final String storePath;
    public final int mbSize;
    public final boolean enabled;


    private int scope(){
        if(name.equals(data)) return Distributable.DATA_SCOPE;
        if(name.equals(integration)) return Distributable.INTEGRATION_SCOPE;
        if(name.equals(index)) return Distributable.INDEX_SCOPE;
        if(name.equals(log)) return Distributable.LOG_SCOPE;
        if(name.equals(local)) return Distributable.LOCAL_SCOPE;
        throw new UnsupportedOperationException("named LMDB env ["+name+"] not supported");
    }

    public Path lib(){
        File file = new File(FileUtil.currentDirectory()+"/target/"+NativeUtil.libName());
        if(file.exists()) return file.toPath();
        FileUtil.createDirectory("target");
        try(InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(NativeUtil.libName());
            FileOutputStream out = new FileOutputStream(FileUtil.currentDirectory()+"/target/"+NativeUtil.libName())){
            in.transferTo(out);
            out.flush();
            return Path.of(FileUtil.currentDirectory()+"/target/"+NativeUtil.libName());
        }catch (Exception ex){
            throw new RuntimeException("No native lib found",ex);
        }
    }

    public static EnvSetting disable(int scope){
        switch (scope){
            case Distributable.DATA_SCOPE -> {
                return new EnvSetting(data,ENV_BASE_DIR+"/data",0,false);
            }
            case Distributable.INTEGRATION_SCOPE -> {
                return new EnvSetting(integration,ENV_BASE_DIR+"/integration",0,false);
            }
            case Distributable.INDEX_SCOPE -> {
                return new EnvSetting(index,ENV_BASE_DIR+"/index",0,false);
            }
            case Distributable.LOG_SCOPE -> {
                return new EnvSetting(log,ENV_BASE_DIR+"/log",0,false);
            }
            case Distributable.LOCAL_SCOPE -> {
                return new EnvSetting(local,ENV_BASE_DIR+"/local",0,false);
            }
            default -> throw new UnsupportedOperationException("scope not supported ["+scope+"]");
        }
    }
    public static EnvSetting setting(int scope,String baseDir,int mbSize){
        switch (scope){
            case Distributable.DATA_SCOPE -> {
                return new EnvSetting(data,baseDir+"/data",mbSize,true);
            }
            case Distributable.INTEGRATION_SCOPE -> {
                return new EnvSetting(integration,baseDir+"/integration",mbSize,true);
            }
            case Distributable.INDEX_SCOPE -> {
                return new EnvSetting(index,baseDir+"/index",mbSize,true);
            }
            case Distributable.LOG_SCOPE -> {
                return new EnvSetting(log,baseDir+"/log",mbSize,true);
            }
            case Distributable.LOCAL_SCOPE -> {
                return new EnvSetting(local,baseDir+"/local",mbSize,true);
            }
            default -> throw new UnsupportedOperationException("scope not supported ["+scope+"]");
        }
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
