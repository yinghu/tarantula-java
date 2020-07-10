package com.tarantula.platform.service;

public interface RecoverService extends ServiceProvider{

    String NAME = "RecoverService";

    byte[] recover(String source,byte[] key);
}
