package com.icodesoftware.service;


import com.icodesoftware.AccessIndex;

public interface AccessIndexService extends ServiceProvider {

    String NAME = "AccessIndexService";

    AccessIndex set(String accessKey,int referenceId);
    AccessIndex setIfAbsent(String accessKey,int referenceId);

    AccessIndex get(String accessKey);



    boolean enable();
    boolean disable();

    int replicate(int partition,byte[] key,byte[] value,int nodeNumber);
    byte[] recover(int partition,byte[] key);


    interface Listener{
        void onStop();
        void onStart();
    }

    interface AccessIndexStore{
        boolean available(byte[] key);
        void setAccessIndex(byte[] key,AccessIndex value);
        AccessIndex getAccessIndex(byte[] key);
    }
}
