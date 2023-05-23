package com.icodesoftware.service;

import com.icodesoftware.EventListener;
import com.icodesoftware.Recoverable;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    ClusterStore clusterStore(String size,String name,boolean map,boolean index,boolean queue);
    ClusterStore clusterStore(String size,String name);


    void registerMetricsListener(MetricsListener metricsListener);
    String registerReloadListener(ReloadListener reloadListener);
    void unregisterReloadListener(String registerKey);

    Node roundRobinMember();

    int partition(byte[] key);

    Summary summary();

    interface ClusterStore{

        String SMALL = "small.";
        String MEDIUM = "medium.";
        String LARGE = "large.";

        //map operations
        String name();
        byte[] mapGet(byte[] key);
        boolean mapExists(byte[] key);
        byte[] mapSetIfAbsent(byte[] key,byte[] pending);
        void mapSet(byte[] key,byte[] value);
        byte[] mapRemove(byte[] key);
        void mapLock(byte[] key);
        void mapUnlock(byte[] key);
        boolean tryMapLock(byte[] key);
        boolean tryMapLock(byte[] key, long time, TimeUnit timeUnit);

        //cluster index operations
        void indexSet(String index,byte[] key);
        void indexRemove(String index,byte[] key);
        Collection<byte[]> indexGet(String index);
        void indexRemove(String index);

        //queue operation
        boolean queueOffer(byte[] value);
        byte[] queuePoll();
        byte[] queuePoll(long time,TimeUnit timeUnit);
        void queueClear();

        void destroy();
        void destroy(boolean map,boolean index,boolean queue);

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
