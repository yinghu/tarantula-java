package com.tarantula.platform.service;

public interface ClusterConfigurationCallback {

    boolean onRegister(String category,String itemId);
    boolean onRelease(String category,String itemId);
}
