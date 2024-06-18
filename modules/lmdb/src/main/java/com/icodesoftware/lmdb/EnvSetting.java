package com.icodesoftware.lmdb;

import com.icodesoftware.Distributable;

public class EnvSetting {

    private static final long MB_1 = 1_048_576L;
    public static final int MAX_LMDB_KEY_SIZE = 511;
    public static final int MAX_STORE_NUMBER = 1024;
    public static final int MAX_READER_NUMBER = 100;
    public static final int KEY_SIZE = 200;//less than 511
    public static final int VALUE_SIZE = 1500; //less than 1521
    public static final int MAX_PENDING_BUFFER_NUMBER = 32;

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
}
