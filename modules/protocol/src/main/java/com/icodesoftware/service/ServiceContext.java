package com.icodesoftware.service;

import com.icodesoftware.*;

import java.util.List;

public interface ServiceContext extends Context{

    //create data scope partitioned data store
    DataStore dataStore(int scope,String name);
    DataStore dataStore(ApplicationSchema applicationSchema,int scope,String name);

    Recoverable.DataBufferPair dataBufferPair();
    EventService eventService();
    ClusterProvider clusterProvider();
    ServiceProvider serviceProvider(String name);
    DeploymentServiceProvider deploymentServiceProvider();
    HttpClientProvider httpClientProvider();
    BackupProvider backupProvider();

    KeyIndexService keyIndexService();
    OnPartition[] partitions();

    ClusterProvider.Node node();
    long distributionId();
    ServiceEventLogger serviceEventLogger();
    <T extends Recoverable> RecoverableRegistry<T> recoverableRegistry(int registryId);
    void recoverableRegistry(RecoverableListener recoverableListener);

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

    Transaction transaction(int scope);

}
