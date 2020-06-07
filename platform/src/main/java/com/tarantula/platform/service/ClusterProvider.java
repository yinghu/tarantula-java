package com.tarantula.platform.service;

import com.tarantula.*;

import java.util.List;

/**
 * Update by yinghu on 4/9/2019.
 */
public interface ClusterProvider extends Serviceable{

    String name();
    int scope();
    String bucket();
    String subscription();
    int size();
    int partitionCount();
    //
    String addEventListener(String registerId,EventListener eventListener);
    void removeEventListener(String registerId);

    //EventListener Register
    EventService subscribe(String topic, EventListener callback);
    void unsubscribe(String topic);

    //CLUSTERING SERVICE
    AccessIndexService accessIndexService();
    DeployService deployService();
    boolean onPartition(byte[] key);

    //CLUSTER KEY VALUE STORE WITH KEY INDEX
    <T extends Recoverable> List<T> list(RecoverableFactory<T> query);
    <T extends Recoverable> void list(RecoverableFactory<T> query, DataStore.Stream<T> stream);
    void set(Metadata metadata,byte[] key,byte[] value);
    byte[] get(byte[] key);
    <T extends Recoverable> boolean load(T t);

    void set(byte[] key,byte[] value);
    void index(String index,byte[] key);
    byte[] firstIndex(String index);
    byte[] remove(byte[] key);

    RecoverableListener registerRecoverableListener(RecoverableListener recoverableListener);
    void unregisterRecoverableListener(int factoryId);
    void registerMetricsListener(MetricsListener metricsListener);


}
