package com.icodesoftware.service;


import com.icodesoftware.AccessIndex;

public interface AccessIndexService extends ServiceProvider {

    String NAME = "AccessIndexService";

    AccessIndex set(String accessKey,int referenceId);
    AccessIndex setIfAbsent(String accessKey,int referenceId);

    AccessIndex get(String accessKey);

    int syncStart(int partition,String syncKey);
    void sync(int size,byte[][] keys,byte[][] values,String memberId,int partition);
    void syncEnd(String memberId,String syncKey);

    boolean enable();
    boolean disable();

    int replicate(int partition,byte[] key,byte[] value,int nodeNumber);
    byte[] recover(int partition,byte[] key);


    interface Listener{
        void onStop();
        void onStart();
    }

    interface AccessIndexStore{
        String STORE_NAME_PREFIX = "p_";
        String name();
        int partitionNumber();
        long count();
        long count(int partition);
    }
}
