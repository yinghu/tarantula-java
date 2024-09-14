package com.tarantula.test;

import com.icodesoftware.AccessIndex;
import com.icodesoftware.service.AccessIndexService;
import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.OnReplication;

public class TestAccessIndexService implements AccessIndexService {


    @Override
    public AccessIndex set(String accessKey, int referenceId) {
        return null;
    }

    @Override
    public AccessIndex setIfAbsent(String accessKey, int referenceId) {
        return null;
    }

    @Override
    public AccessIndex get(String accessKey) {
        return null;
    }

    @Override
    public boolean delete(String accessKey) {
        return false;
    }

    @Override
    public boolean onEnable() {
        return false;
    }

    @Override
    public boolean onDisable() {
        return false;
    }



    @Override
    public byte[] onRecover( byte[] key) {
        return  null;
    }


    @Override
    public String name() {
        return null;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
}
