package com.icodesoftware.service;

import com.icodesoftware.*;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

public interface ServiceContext {

    //create data scope partitioned data store
    DataStore dataStore(String name, int partition);

    ScheduledFuture<?> schedule(SchedulingTask task);
    EventService eventService(int scope);
    ClusterProvider clusterProvider(int scope);
    ServiceProvider serviceProvider(String name);
    DeploymentServiceProvider deploymentServiceProvider();
    AccessIndexService accessIndexService();
    TarantulaLogger logger(Class c);
    OnPartition[] partitions();
    int partitionNumber();

    String bucket();
    String bucketId();
    String nodeId();

    RecoverableRegistry recoverableRegistry(int registryId);
    TokenValidatorProvider.AuthVendor authVendor(String name);
    Configuration configuration(String config);
    <T extends OnAccess> void setup(T configuration);
    List<Descriptor> availableServices();
}
