package com.tarantula.platform.service;

public interface RecoverService extends ServiceProvider{

    String NAME = "RecoverService";

    byte[] recover(String source,byte[] key);
    void replicate(String source,byte[] key,byte[] value);


}
