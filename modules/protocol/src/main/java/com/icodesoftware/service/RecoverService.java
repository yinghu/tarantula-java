package com.icodesoftware.service;

public interface RecoverService extends ServiceProvider{

    String NAME = "RecoverService";

    //operates master or member node
    void onDelete(String source,byte[] key);
    void onDeleteEdge(String source,String label,byte[] key);
    void onDeleteEdge(String source,String label,byte[] key,byte[] edge);
    byte[] onRecover(String source,byte[] key,ClusterProvider.Node[] nodes);
    Batchable onRecover(String source,String label,byte[] key,ClusterProvider.Node[] nodes);

    int onReplicate(String nodeName,String source,String label, byte[] key, byte[] value, ClusterProvider.Node[] nodes);
    void onReplicate(String nodeName,OnReplication[] batch, int size, ClusterProvider.Node node);

    //operates on master node
    int onStartSync(String source,String syncKey);
    void onSync(int size,byte[][] keys,byte[][] values,String memberId,String source);
    void onEndSync(String memberId,String syncKey);

    String[] onListModules();
    byte[] onLoadModuleJarFile(String name);

}
