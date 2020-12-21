package com.icodesoftware.service;

public interface RecoverService extends ServiceProvider{

    String NAME = "RecoverService";

    boolean queryStart(String source,String dataStore,int factorId,int classId,String[] params);
    void query(String memberId,String source,byte[] key,byte[] value);
    void queryEnd(String memberId,String source);

    byte[] load(String dataSource,byte[] key);
    byte[] recover(String source,byte[] key);
    void replicate(String source,int partition,byte[] key,byte[] value);

    int syncStart(String source);
    void sync(int size,byte[][] keys,byte[][] values,String memberId,String source);
    void syncEnd(String memberId);

    interface QueryCallback{
        void on(byte[] key,byte[] value);
    }
    interface QueryEndCallback{
        void on();
    }
}
