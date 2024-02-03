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
    public int onStartSync(int partition, String syncKey) {
        return 0;
    }

    @Override
    public void onSync(int size, byte[][] keys, byte[][] values, String memberId, int partition) {

    }

    @Override
    public void onEndSync(String memberId, String syncKey) {

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
    public int onReplicate(String nodeName,byte[] key, byte[] value,  ClusterProvider.Node[] nodes) {
        return 0;
    }

    @Override
    public void onReplicate(String nodeName,OnReplication[] batch, int size, ClusterProvider.Node node) {
        //return 0;
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
