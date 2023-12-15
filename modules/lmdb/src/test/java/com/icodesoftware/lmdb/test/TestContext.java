package com.icodesoftware.lmdb.test;

import com.icodesoftware.*;
import com.icodesoftware.lmdb.LMDBDataStoreProvider;
import com.icodesoftware.service.*;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class TestContext implements ServiceContext {

    public LMDBDataStoreProvider lmdbDataStoreProvider;
    public TestNode testNode = new TestNode();
    @Override
    public ScheduledFuture<?> schedule(SchedulingTask task) {
        return null;
    }

    @Override
    public void log(String message, int level) {

    }

    @Override
    public void log(String message, Exception error, int level) {

    }

    @Override
    public DataStore dataStore(int scope, String name) {
        if(scope==Distributable.DATA_SCOPE){
            return lmdbDataStoreProvider.createDataStore(name);
        }
        if(scope==Distributable.INTEGRATION_SCOPE){
            return lmdbDataStoreProvider.createAccessIndexDataStore(name);
        }
        if(scope==Distributable.LOG_SCOPE){
            return lmdbDataStoreProvider.createLogDataStore(name);
        }
        if(scope==Distributable.INDEX_SCOPE){
            return lmdbDataStoreProvider.createKeyIndexDataStore(name);
        }
        return null;
    }

    @Override
    public EventService eventService() {
        return null;
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
    public HttpClientProvider httpClientProvider() {
        return null;
    }

    @Override
    public BackupProvider backupProvider() {
        return null;
    }

    @Override
    public KeyIndexService keyIndexService() {
        return null;
    }

    @Override
    public OnPartition[] partitions() {
        return new OnPartition[0];
    }

    @Override
    public ClusterProvider.Node node() {
        return testNode;
    }

    @Override
    public long distributionId() {
        return 0;
    }


    @Override
    public <T extends Recoverable> RecoverableRegistry<T> recoverableRegistry(int registryId) {
        return null;
    }

    @Override
    public void recoverableRegistry(RecoverableListener recoverableListener) {

    }

    //@Override
    public TokenValidatorProvider.AuthVendor authVendor(String name) {
        return null;
    }

    @Override
    public Configuration configuration(String config) {
        return null;
    }

    @Override
    public List<Descriptor> availableServices() {
        return null;
    }

    @Override
    public void registerAuthVendor(TokenValidatorProvider.AuthVendor authVendor) {

    }

    @Override
    public void unregisterAuthVendor(TokenValidatorProvider.AuthVendor authVendor) {

    }

    @Override
    public void registerBackupProvider(BackupProvider backupProvider) {

    }

    @Override
    public void unregisterBackupProvider(BackupProvider backupProvider) {

    }

    @Override
    public Metrics metrics(String name) {
        return null;
    }

    @Override
    public void registerMetrics(Metrics metrics) {

    }

    @Override
    public void unregisterMetrics(Metrics metrics) {

    }

    @Override
    public PostOffice postOffice() {
        return null;
    }

    @Override
    public Transaction transaction(int scope) {
        return null;
    }

    public Recoverable.DataBufferPair dataBufferPair(){
        return null;
    }

    public DataStore dataStore(ApplicationSchema applicationSchema,int scope,String name){
        return null;
    }
}
