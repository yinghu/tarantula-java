package com.icodesoftware.service;


import com.icodesoftware.AccessIndex;

public interface AccessIndexService extends ServiceProvider {

    String NAME = "AccessIndexService";

    AccessIndex set(String accessKey,int referenceId);
    AccessIndex setIfAbsent(String accessKey,int referenceId);

    AccessIndex get(String accessKey);

    int onStartSync(int partition,String syncKey);
    void onSync(int size,byte[][] keys,byte[][] values,String memberId,int partition);
    void onEndSync(String memberId,String syncKey);

    boolean onEnable();
    boolean onDisable();

    int onReplicate(int partition,byte[] key,byte[] value,int nodeNumber);
    byte[] onRecover(int partition,byte[] key);


    interface Listener{
        void onStop();
        void onStart();
    }

    interface AccessIndexStore{
        String STORE_NAME_PREFIX = "tarantula_";
        String name();
        int partitionNumber();
        long count();
        long count(int partition);
    }
}
