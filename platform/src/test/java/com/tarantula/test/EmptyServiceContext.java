package com.tarantula.test;

import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.tarantula.platform.util.SystemUtil;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class EmptyServiceContext implements ServiceContext {
    @Override
    public DataStore dataStore(String s, int i) {
        return new EmptyDataStore();
    }

    @Override
    public ScheduledFuture<?> schedule(SchedulingTask schedulingTask) {
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
    public ServiceProvider serviceProvider(String s) {
        return null;
    }

    @Override
    public DeploymentServiceProvider deploymentServiceProvider() {
        return null;
    }

    @Override
    public AccessIndexService accessIndexService() {
        return null;
    }

    @Override
    public TarantulaLogger logger(Class aClass) {

        return new EmptyLogger();
    }

    @Override
    public OnPartition[] partitions() {
        return new OnPartition[0];
    }

    @Override
    public int partitionNumber() {
        return 0;
    }

    @Override
    public String clusterNameSuffix() {
        return null;
    }

    @Override
    public String bucket() {
        return null;
    }

    @Override
    public String bucketId() {
        return "BSD01/"+ SystemUtil.oid();
    }

    @Override
    public String nodeId() {
        return "BSD01/"+ SystemUtil.oid();
    }

    @Override
    public String servicePushAddress() {
        return null;
    }

    @Override
    public String deployDirectory() {
        return null;
    }

    @Override
    public RecoverableRegistry recoverableRegistry(int i) {
        return null;
    }

    @Override
    public TokenValidatorProvider.AuthVendor authVendor(String s) {
        return null;
    }

    @Override
    public Configuration configuration(String s) {
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
    public Metrics metrics(String s) {
        return null;
    }

    @Override
    public void registerMetrics(Metrics metrics) {

    }

    @Override
    public void unregisterMetrics(Metrics metrics) {

    }

    public void registerBackupProvider(BackupProvider backupProvider){}
    public void unregisterBackupProvider(BackupProvider backupProvider){}
}
