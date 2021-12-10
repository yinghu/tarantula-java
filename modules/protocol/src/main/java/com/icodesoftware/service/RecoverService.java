package com.icodesoftware.service;

import com.icodesoftware.Access;

public interface RecoverService extends ServiceProvider{

    String NAME = "RecoverService";
    int ROLE_MATCHED = 0;
    int ROLE_NOT_MATCHED = 1;
    int CHECK_SKIPPED = 2;

    //operates master or member node
    boolean checkAccessControl(String systemId, Access.Role role);
    String findDataNode(String source,byte[] key);
    byte[] load(String memberId,String dataSource,byte[] key);

    //operates on other nodes
    byte[] recover(String source,byte[] key);
    int replicate(String source,int partition,byte[] key,byte[] value,int nodeNumber);

    //operates on master node
    int syncStart(String source,String syncKey);
    void sync(int size,byte[][] keys,byte[][] values,String memberId,String source);
    void syncEnd(String memberId,String syncKey);

    String[] listModules();
    byte[] loadModuleJarFile(String name);

}
