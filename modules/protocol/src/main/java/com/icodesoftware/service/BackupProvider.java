package com.icodesoftware.service;

import com.icodesoftware.Recoverable;

import java.util.Map;

public interface BackupProvider extends ServiceProvider {

    boolean enabled();
    void enabled(boolean enabled);
    int scope();

    void registerDataStore(String name);
    void registerDataStore(String prefix,int partitions);

    void configure(Map<String,Object> properties);

    <T extends Recoverable> void update(Metadata metadata, String key, T t);
    <T extends Recoverable> void create(Metadata metadata, String key, T t);

}
