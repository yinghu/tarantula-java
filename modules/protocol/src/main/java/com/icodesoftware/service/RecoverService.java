package com.icodesoftware.service;

public interface RecoverService extends ServiceProvider{

    String NAME = "RecoverService";

    //operates master or member node

    byte[] onRecover(String source,byte[] key);
    int onReplicate(String source,byte[] key,byte[] value,int nodeNumber);

    //operates on master node
    int onStartSync(String source,String syncKey);
    void onSync(int size,byte[][] keys,byte[][] values,String memberId,String source);
    void onEndSync(String memberId,String syncKey);

    String[] onListModules();
    byte[] onLoadModuleJarFile(String name);

}
