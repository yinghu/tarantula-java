package com.icodesoftware.lmdb;

public class EnvSetting {
    
    public static final String data ="data";
    public static final String integration ="integration";
    public static final String index ="index";
    public static final String log ="log";
    public static final String local ="local";

    public static final EnvSetting DataSetting = new EnvSetting(data,"target/lmdb/data",0,true);
    public static final EnvSetting IntegrationSetting = new EnvSetting(integration,"target/lmdb/integration",0,true);
    public static final EnvSetting IndexSetting = new EnvSetting(index,"target/lmdb/index",0,true);
    public static final EnvSetting LogSetting = new EnvSetting(log,"target/lmdb/log",0,true);
    public static final EnvSetting LocalSetting = new EnvSetting(local,"target/lmdb/local",0,true);

    public EnvSetting(String name,String storePath,long mbSize,boolean enabled){
        this.name = name;
        this.storePath = storePath;
        this.mbSize = mbSize;
        this.enabled = enabled;
    }
    public String name;
    public String storePath;
    public long mbSize;
    public boolean enabled;

}
