package com.icodesoftware.service;

import com.icodesoftware.*;

import java.util.List;

public interface ServiceContext extends Context{

    //create data scope partitioned data store

    DataStore dataStore(ApplicationSchema applicationSchema,int scope,String name);

    Recoverable.DataBufferPair dataBufferPair();
    EventService eventService();
    ClusterProvider clusterProvider();
    ServiceProvider serviceProvider(String name);
    DeploymentServiceProvider deploymentServiceProvider();
    HttpClientProvider httpClientProvider();

    OnPartition[] partitions();
    OnPartition[] buckets();


    <T extends Recoverable> RecoverableRegistry<T> recoverableRegistry(int registryId);
    void recoverableRegistry(RecoverableListener recoverableListener);
    default TokenValidator tokenValidator(){
        return null;
    }
    Configuration configuration(String config);
    List<Descriptor> availableServices();

    void registerAuthVendor(TokenValidatorProvider.AuthVendor authVendor);
    void unregisterAuthVendor(TokenValidatorProvider.AuthVendor authVendor);

    Metrics metrics(String name);
    List<String> metricsList();
    void registerMetrics(Metrics metrics);
    void unregisterMetrics(Metrics metrics);


}
