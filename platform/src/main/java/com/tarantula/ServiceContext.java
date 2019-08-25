package com.tarantula;

import com.tarantula.platform.service.ClusterProvider;

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
    AccessIndexService accessIndexService();
    TarantulaLogger logger(Class c);
    OnPartition[] partitions();
    int partitionNumber();
    RecoverableRegistry recoverableRegistry(int registryId);
}
