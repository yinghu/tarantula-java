package com.tarantula.platform.service;

import com.tarantula.*;

import java.util.concurrent.ScheduledFuture;

/**
 * Updated by yinghu lu on 6/15/19
 */
public interface ServiceContext {

    DataStore dataStore(String name);
    DataStore dataStore(String name,int partition);
    ScheduledFuture<?> schedule(SchedulingTask task);
    EventService eventService(int scope);
    ClusterProvider clusterProvider(int scope);
    ServiceProvider serviceProvider(String name);
    DeploymentServiceProvider deploymentServiceProvider();
    AccessIndexService accessIndexService();
    TarantulaLogger logger(Class c);
    OnPartition[] partitions();
    int partitionNumber();
    Connection endpoint();
    String bucket();
    RecoverableRegistry recoverableRegistry(int registryId);
    TokenValidatorProvider.AuthVendor authVendor(String name);
    //void onDataStore(DataStore dataStore);

}
