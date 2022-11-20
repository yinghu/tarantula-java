package com.icodesoftware.service;


import java.util.Map;

public interface BackupProvider extends ServiceProvider {

    boolean enabled();
    void enabled(boolean enabled);
    int scope();

    void registerDataStore(int scope,String name);

    void configure(Map<String,Object> properties);

    void batch(OnReplication[] onReplications,int size);

}
