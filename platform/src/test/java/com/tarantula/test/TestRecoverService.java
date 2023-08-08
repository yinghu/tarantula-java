package com.tarantula.test;

import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.OnReplication;
import com.icodesoftware.service.RecoverService;

public class TestRecoverService implements RecoverService {
    @Override
    public byte[] onRecover(String source, byte[] key,ClusterProvider.Node[] nodes) {
        return null;
    }

    @Override
    public int onReplicate(String source, byte[] key, byte[] value, ClusterProvider.Node[] nodes) {
        return 0;
    }

    @Override
    public int onReplicate(OnReplication[] batch, int size, int nodeNumber) {
        return 0;
    }

    @Override
    public int onStartSync(String source, String syncKey) {
        return 0;
    }

    @Override
    public void onSync(int size, byte[][] keys, byte[][] values, String memberId, String source) {

    }

    @Override
    public void onDelete(String source, byte[] key) {
        
    }

    @Override
    public void onEndSync(String memberId, String syncKey) {

    }

    @Override
    public String[] onListModules() {
        return new String[0];
    }


    @Override
    public byte[] onLoadModuleJarFile(String name) {
        return new byte[0];
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
