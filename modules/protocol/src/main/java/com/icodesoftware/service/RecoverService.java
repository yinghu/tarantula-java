package com.icodesoftware.service;

public interface RecoverService extends ServiceProvider{

    String NAME = "RecoverService";

    byte[] recover(String source,byte[] key);
    void replicate(String source,int partition,byte[] key,byte[] value);

    int syncStart(String source);
    void sync(int size,byte[][] keys,byte[][] values,String memberId,String source);
    void syncEnd(String memberId);
}
