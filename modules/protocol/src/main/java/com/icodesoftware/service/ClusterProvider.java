package com.icodesoftware.service;

import com.icodesoftware.EventListener;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface ClusterProvider extends Serviceable {

    String name();
    int scope();
    String bucket();

    //EventListener Register
    EventService publisher();
    EventService subscribe(String topic, EventListener callback);
    void unsubscribe(String topic);

    //CLUSTERING SERVICE
    AccessIndexService accessIndexService();
    DeployService deployService();
    RecoverService recoverService();

    <T extends ServiceProvider> T serviceProvider(String name);

    ClusterStore clusterStore(String name);
    //CLUSTER KEY VALUE
    byte[] get(byte[] key);
    byte[] createIfAbsent(byte[] key,byte[] pending);
    void set(byte[] key,byte[] value);
    byte[] remove(byte[] key);

    //CLUSTER INDEX
    void index(String index,byte[] key);
    void removeIndex(String index,byte[] key);
    Collection<byte[]> index(String index);
    void removeIndex(String index);

    void registerMetricsListener(MetricsListener metricsListener);
    String registerReloadListener(ReloadListener reloadListener);
    void unregisterReloadListener(String registerKey);

    interface ClusterStore{
        byte[] get(byte[] key);
        byte[] createIfAbsent(byte[] key,byte[] pending);
        void set(byte[] key,byte[] value);
        byte[] remove(byte[] key);

        //CLUSTER INDEX
        void index(String index,byte[] key);
        void removeIndex(String index,byte[] key);
        Collection<byte[]> index(String index);
        void removeIndex(String index);

        void lock(byte[] key);
        void unlock(byte[] key);

    }

    interface Summary{
        String clusterName();
        LocalDateTime startTime();
        List<Node> clusterNodes();
    }

    interface Node{

        String bucketName();
        String nodeName();
        String bucketId();
        String nodeId();

        String memberId();
        LocalDateTime startTime();

    }

}
