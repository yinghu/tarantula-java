package com.icodesoftware.service;

import com.icodesoftware.Recoverable;

public interface BackupProvider extends ServiceProvider {

    boolean enabled();
    void enabled(boolean enabled);
    int scope();

    void registerDataStore(String name);
    void registerDataStore(String prefix,int partitions);


    <T extends Recoverable> void update(Metadata metadata, String key, T t);
    <T extends Recoverable> void create(Metadata metadata, String key, T t);

}
