package com.tarantula.platform.service;


import com.tarantula.AccessIndex;

/**
 * Updated by yinghu lu on 6/18/2020
 */
public interface AccessIndexService extends ServiceProvider {

    String NAME = "AccessIndexService";

    AccessIndex set(String accessKey);

    AccessIndex get(String accessKey);

    boolean update(boolean state);

    interface Listener{
        void onStop();
        void onStart();
    }
}
