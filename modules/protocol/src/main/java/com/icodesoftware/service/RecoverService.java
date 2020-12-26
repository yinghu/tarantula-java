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
    boolean queryStart(String memberId,String source,String dataStore,int factorId,int classId,String[] params);
    void query(String memberId,String source,byte[] key,byte[] value);
    void queryEnd(String memberId,String source);

    //operates on other nodes
    byte[] recover(String source,byte[] key);
    void replicate(String source,int partition,byte[] key,byte[] value);

    //operates on master node
    int syncStart(String source);
    void sync(int size,byte[][] keys,byte[][] values,String memberId,String source);
    void syncEnd(String memberId);

    String[] listModules();
    byte[] loadModuleJarFile(String name);
    byte[] loadModuleIndex();
    byte[] loadGameClusterIndex();
    byte[] findTypeIdIndex(String typeId);

    interface QueryCallback{
        void on(byte[] key,byte[] value);
    }
    interface QueryEndCallback{
        void on();
    }
}
