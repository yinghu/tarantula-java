package com.icodesoftware.service;


import com.icodesoftware.AccessIndex;

import java.util.List;

public interface AccessIndexService extends ServiceProvider {

    String NAME = "AccessIndexService";
    String STORE_NAME = "tarantula_access_index";
    AccessIndex set(String accessKey,int referenceId);
    AccessIndex setIfAbsent(String accessKey,int referenceId);

    AccessIndex get(String accessKey);
    List<Boolean> delete(String accessKey);

    boolean onEnable();
    boolean onDisable();

    byte[] onRecover(byte[] key);


    interface Listener{
        void onStop();
        void onStart();
    }

}
