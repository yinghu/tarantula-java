package com.icodesoftware.service;

public interface RecoverService extends ServiceProvider{

    String NAME = "RecoverService";

    //operates master or member node

    byte[] recover(String source,byte[] key);
    int replicate(String source,byte[] key,byte[] value,int nodeNumber);

    //operates on master node
    int syncStart(String source,String syncKey);
    void sync(int size,byte[][] keys,byte[][] values,String memberId,String source);
    void syncEnd(String memberId,String syncKey);

    String[] listModules();
    byte[] loadModuleJarFile(String name);

}
