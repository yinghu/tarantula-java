package com.icodesoftware.lmdb;

public class EnvSetting {

    public static final String data ="data";
    public static final String integration ="integration";
    public static final String index ="index";
    public static final String log ="log";
    public static final String local ="local";

    public static final EnvSetting DataSetting = new EnvSetting("target/lmdb/data",0,true);
    public static final EnvSetting IntegrationSetting = new EnvSetting("target/lmdb/integration",0,true);
    public static final EnvSetting IndexSetting = new EnvSetting("target/lmdb/index",0,true);
    public static final EnvSetting LogSetting = new EnvSetting("target/lmdb/log",0,true);
    public static final EnvSetting LocalSetting = new EnvSetting("target/lmdb/local",0,true);

    public EnvSetting(String storePath,long mbSize,boolean enabled){
        this.storePath = storePath;
        this.mbSize = mbSize;
        this.enabled = enabled;
    }

    public String storePath;
    public long mbSize;
    public boolean enabled;

}
