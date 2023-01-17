package com.icodesoftware.service;

import com.icodesoftware.EventListener;
import com.icodesoftware.Recoverable;

import java.util.Collection;
import java.util.List;

public interface ClusterProvider extends ServiceProvider {

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

    ClusterStore clusterStore(String name,boolean map,boolean index,boolean queue);
    ClusterStore clusterStore(String name);


    void registerMetricsListener(MetricsListener metricsListener);
    String registerReloadListener(ReloadListener reloadListener);
    void unregisterReloadListener(String registerKey);

    Node roundRobinMember();

    Summary summary();

    interface ClusterStore{

        //map operations
        String name();
        byte[] mapGet(byte[] key);
        byte[] mapCreateIfAbsent(byte[] key,byte[] pending);
        void mapSet(byte[] key,byte[] value);
        byte[] mapRemove(byte[] key);
        void mapLock(byte[] key);
        void mapUnlock(byte[] key);


        //cluster index operations
        void indexSet(String index,byte[] key);
        void indexRemove(String index,byte[] key);
        Collection<byte[]> indexGet(String index);
        void indexRemove(String index);

        //queue operation
        boolean queueOffer(byte[] value);
        byte[] queuePoll();

        void clear();
        void clear(boolean map,boolean index,boolean queue);

    }

    interface Summary extends Recoverable{
        String clusterName();
        int partitionNumber();
        List<Node> clusterNodes();
    }

    interface Node extends Recoverable {

        String bucketName();
        String nodeName();
        String bucketId();
        String nodeId();
        String memberId();
        String address();
        long startTime();

        String deploymentId();

        int partitionNumber();

        String clusterNameSuffix();

        String deployDirectory();

        String servicePushAddress();

        boolean runAsMirror();
        boolean backupEnabled();
        boolean dailyBackupEnabled();
        String dataStoreDirectory();

    }

}
