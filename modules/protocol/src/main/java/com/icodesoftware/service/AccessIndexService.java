package com.icodesoftware.service;


import com.icodesoftware.AccessIndex;

public interface AccessIndexService extends ServiceProvider {

    String NAME = "AccessIndexService";

    AccessIndex set(String accessKey,int referenceId);
    AccessIndex setIfAbsent(String accessKey,int referenceId);

    AccessIndex get(String accessKey);



    boolean enable();
    boolean disable();

    boolean replicate(int partition,byte[] key,byte[] value);
    byte[] recover(int partition,byte[] key);

    int syncStart();
    void sync(int size,byte[][] keys,byte[][] values,String memberId);
    void syncEnd(String memberId);

    interface Listener{
        void onStop();
        void onStart();
    }
}
