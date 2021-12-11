package com.icodesoftware.service;

import com.icodesoftware.DataStore;
import com.icodesoftware.EventListener;
import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;

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

    //CLUSTER KEY VALUE STORE WITH KEY INDEX
    <T extends Recoverable> List<T> list(RecoverableFactory<T> query);
    <T extends Recoverable> void list(RecoverableFactory<T> query, DataStore.Stream<T> stream);
    void set(Metadata metadata, byte[] key, byte[] value);
    byte[] get(byte[] key);
    byte[] createIfAbsent(byte[] key,byte[] pending);
    <T extends Recoverable> boolean load(T t);

    void set(byte[] key,byte[] value);
    void index(String index,byte[] key);
    byte[] firstIndex(String index);
    void removeIndex(String index);
    byte[] remove(byte[] key);

    void registerMetricsListener(MetricsListener metricsListener);
    String registerReloadListener(ReloadListener reloadListener);
    void unregisterReloadListener(String registerKey);

}
