package com.icodesoftware.service;

public interface RecoverService extends ServiceProvider{

    String NAME = "RecoverService";

    //operates master or member node
    boolean onDelete(String source,byte[] key);
    boolean onDeleteEdge(String source,String label,byte[] key);
    boolean onDeleteEdge(String source,String label,byte[] key,byte[] edge);
    byte[] onRecover(String source,byte[] key);
    Batchable onRecover(String source,String label,byte[] key);

    String[] onListModules();
    byte[] onLoadModuleJarFile(String name);

}
