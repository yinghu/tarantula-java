package com.icodesoftware.service;

import com.icodesoftware.*;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

public interface ServiceContext extends Context{

    //create data scope partitioned data store
    DataStore dataStore(String name, int partition);

    EventService eventService();
    ClusterProvider clusterProvider();
    ServiceProvider serviceProvider(String name);
    DeploymentServiceProvider deploymentServiceProvider();
    HttpClientProvider httpClientProvider();
    BackupProvider backupProvider();
    //AccessIndexService accessIndexService();

    KeyIndexService keyIndexService();
    OnPartition[] partitions();

    ClusterProvider.Node node();

    ServiceEventLogger serviceEventLogger();
    RecoverableRegistry recoverableRegistry(int registryId);
    TokenValidatorProvider.AuthVendor authVendor(String name);
    Configuration configuration(String config);
    List<Descriptor> availableServices();

    void registerAuthVendor(TokenValidatorProvider.AuthVendor authVendor);
    void unregisterAuthVendor(TokenValidatorProvider.AuthVendor authVendor);

    void registerBackupProvider(BackupProvider backupProvider);
    void unregisterBackupProvider(BackupProvider backupProvider);

    Metrics metrics(String name);
    void registerMetrics(Metrics metrics);
    void unregisterMetrics(Metrics metrics);

    PostOffice postOffice();

}
