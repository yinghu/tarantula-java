package com.tarantula.platform.service;


import com.icodesoftware.AccessIndex;

/**
 * Updated by yinghu lu on 6/18/2020
 */
public interface AccessIndexService extends ServiceProvider {

    String NAME = "AccessIndexService";

    AccessIndex set(String accessKey);

    AccessIndex get(String accessKey);

    boolean enable();
    boolean disable();

    boolean replicate(int partition,byte[] key,byte[] value);
    byte[] recover(int partition,byte[] key);

    interface Listener{
        void onStop();
        void onStart();
    }
}
