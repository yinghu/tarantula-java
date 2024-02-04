package com.tarantula.test;

import com.icodesoftware.service.Batchable;
import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.OnReplication;
import com.icodesoftware.service.RecoverService;

public class TestRecoverService implements RecoverService {
    @Override
    public byte[] onRecover(String source, byte[] key) {
        return null;
    }

    @Override
    public Batchable onRecover(String source, String label, byte[] key) {
        return null;
    }


    @Override
    public boolean onDelete(String source,byte[] key) {
        return false;
    }

    public boolean onDeleteEdge(String source,String label,byte[] key){
        return false;
    }
    public boolean onDeleteEdge(String source,String label,byte[] key,byte[] edge){
        return false;
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
