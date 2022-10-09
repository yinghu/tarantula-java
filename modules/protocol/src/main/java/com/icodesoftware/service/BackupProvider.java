package com.icodesoftware.service;

import com.icodesoftware.Recoverable;

import java.util.Map;

public interface BackupProvider extends ServiceProvider {

    boolean enabled();

    int scope();

    void configure(Map<String,String> properties);
    void registerDataStore(String name);
    void registerDataStore(String prefix,int partitions);


    <T extends Recoverable> void update(Metadata metadata, String key, T t);
    <T extends Recoverable> void create(Metadata metadata, String key, T t);

}
