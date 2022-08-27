package com.icodesoftware.service;

import com.icodesoftware.*;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

public interface ServiceContext {

    //create data scope partitioned data store
    DataStore dataStore(String name, int partition);

    ScheduledFuture<?> schedule(SchedulingTask task);
    EventService eventService();
    ClusterProvider clusterProvider();
    ServiceProvider serviceProvider(String name);
    DeploymentServiceProvider deploymentServiceProvider();
    AccessIndexService accessIndexService();
    TarantulaLogger logger(Class c);
    OnPartition[] partitions();
    int partitionNumber();

    String clusterNameSuffix();
    String bucket();
    String bucketId();
    String nodeId();

    String servicePushAddress();

    String deployDirectory();

    RecoverableRegistry recoverableRegistry(int registryId);
    TokenValidatorProvider.AuthVendor authVendor(String name);
    Configuration configuration(String config);
    List<Descriptor> availableServices();

    void registerAuthVendor(TokenValidatorProvider.AuthVendor authVendor);
    void unregisterAuthVendor(TokenValidatorProvider.AuthVendor authVendor);

    Metrics metrics(String name);
    void registerMetrics(Metrics metrics);
    void unregisterMetrics(Metrics metrics);
}
