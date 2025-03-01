package com.icodesoftware.protocol;

import com.icodesoftware.*;
import com.icodesoftware.service.*;

import java.util.List;


public class TRServiceContext extends TRContext implements ServiceContext {

    protected EventService eventService;

    @Override
    public DataStore dataStore(ApplicationSchema applicationSchema, int scope, String name) {
        return null;
    }

    @Override
    public Recoverable.DataBufferPair dataBufferPair() {
        return dataStoreProvider.dataBufferPair();
    }

    @Override
    public EventService eventService() {
        return eventService;
    }

    @Override
    public ClusterProvider clusterProvider() {
        return null;
    }

    @Override
    public ServiceProvider serviceProvider(String name) {
        return null;
    }

    @Override
    public DeploymentServiceProvider deploymentServiceProvider() {
        return null;
    }


    @Override
    public OnPartition[] partitions() {
        return new OnPartition[0];
    }

    @Override
    public OnPartition[] buckets() {
        return new OnPartition[0];
    }


    @Override
    public <T extends Recoverable> RecoverableRegistry<T> recoverableRegistry(int registryId) {
        return null;
    }

    @Override
    public void recoverableRegistry(RecoverableListener recoverableListener) {

    }

    @Override
    public Configuration configuration(String config) {
        return null;
    }

    @Override
    public List<Descriptor> availableServices() {
        return List.of();
    }

    @Override
    public void registerAuthVendor(TokenValidatorProvider.AuthVendor authVendor) {

    }

    @Override
    public void unregisterAuthVendor(TokenValidatorProvider.AuthVendor authVendor) {

    }

    @Override
    public Metrics metrics(String name) {
        return null;
    }

    @Override
    public List<String> metricsList() {
        return List.of();
    }

    @Override
    public void registerMetrics(Metrics metrics) {

    }

    @Override
    public void unregisterMetrics(Metrics metrics) {

    }

}
