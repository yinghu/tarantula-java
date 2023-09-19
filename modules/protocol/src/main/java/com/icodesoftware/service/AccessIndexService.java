package com.icodesoftware.service;


import com.icodesoftware.AccessIndex;
import com.icodesoftware.DataStore;

public interface AccessIndexService extends ServiceProvider {

    String NAME = "AccessIndexService";

    AccessIndex set(String accessKey,int referenceId);
    AccessIndex setIfAbsent(String accessKey,int referenceId);

    AccessIndex get(String accessKey);

    int onStartSync(int partition,String syncKey);
    void onSync(int size,byte[][] keys,byte[][] values,String memberId,int partition);
    void onEndSync(String memberId,String syncKey);

    boolean onEnable();
    boolean onDisable();

    int onReplicate(String nodeName,byte[] key, byte[] value, ClusterProvider.Node[] nodes);
    void onReplicate(String nodeName,OnReplication[] batch, int size, ClusterProvider.Node node);
    byte[] onRecover(String source,byte[] key,ClusterProvider.Node[] nodes);


    interface Listener{
        void onStop();
        void onStart();
    }

    interface AccessIndexStore extends DataStoreSummary {
        String STORE_NAME = "tarantula_access_index";

    }
}
