package com.tarantula.platform.service.deployment;

import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.tarantula.platform.TarantulaContext;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class ServiceContextProxy implements ServiceContext {

    private TarantulaContext tarantulaContext;

    public ServiceContextProxy(TarantulaContext tarantulaContext){
        this.tarantulaContext = tarantulaContext;
    }

    @Override
    public DataStore dataStore(String name, int partition) {
        return this.tarantulaContext.dataStore(name,partition);
    }

    @Override
    public ScheduledFuture<?> schedule(SchedulingTask task) {
        return this.tarantulaContext.schedule(task);
    }

    @Override
    public EventService eventService() {
        return this.tarantulaContext.eventService();
    }

    @Override
    public ClusterProvider clusterProvider() {
        return this.tarantulaContext.clusterProvider();
    }

    @Override
    public ServiceProvider serviceProvider(String name) {
        return this.tarantulaContext.serviceProvider(name);
    }

    @Override
    public AccessIndexService accessIndexService() {
        return this.tarantulaContext.accessIndexService();
    }

    @Override
    public TarantulaLogger logger(Class c) {
        return this.tarantulaContext.logger(c);
    }

    @Override
    public OnPartition[] partitions() {
        return this.tarantulaContext.partitions();
    }

    @Override
    public int partitionNumber() {
        return this.tarantulaContext.partitionNumber();
    }

    public String clusterNameSuffix(){
        return this.tarantulaContext.clusterNameSuffix();
    }
    @Override
    public String bucket() {
        return this.tarantulaContext.bucket();
    }
    public String bucketId(){
        return this.tarantulaContext.bucketId();
    }
    public String nodeId(){
        return this.tarantulaContext.nodeId();
    }

    public ClusterProvider.Node node(){
        return this.tarantulaContext.node();
    }

    @Override
    public RecoverableRegistry recoverableRegistry(int registryId) {
        return this.tarantulaContext.recoverableRegistry(registryId);
    }
    public TokenValidatorProvider.AuthVendor authVendor(String name){
        return this.tarantulaContext.authVendor(name);
    }
    @Override
    public DeploymentServiceProvider deploymentServiceProvider(){
        return this.tarantulaContext.deploymentServiceProvider();
    }
    public Configuration configuration(String config){
        return this.tarantulaContext.configuration(config);
    }
    public List<Descriptor> availableServices(){
        return this.tarantulaContext.availableServices();
    }

    public String deployDirectory(){
        return this.tarantulaContext.deployDirectory();
    }

    public String servicePushAddress(){
        return tarantulaContext.servicePushAddress();
    }

    public void registerAuthVendor(TokenValidatorProvider.AuthVendor authVendor){
        this.tarantulaContext.registerAuthVendor(authVendor);
    }
    public void unregisterAuthVendor(TokenValidatorProvider.AuthVendor authVendor){
        this.tarantulaContext.unregisterAuthVendor(authVendor);
    }
    public Metrics metrics(String name){
        return this.tarantulaContext.metrics(name);
    }

    public void registerMetrics(Metrics metrics){
        this.tarantulaContext.registerMetrics(metrics);
    }
    public void unregisterMetrics(Metrics metrics){
        this.tarantulaContext.unregisterMetrics(metrics);
    }

    public void registerBackupProvider(BackupProvider backupProvider){
        this.tarantulaContext.registerBackupProvider(backupProvider);
    }
    public void unregisterBackupProvider(BackupProvider backupProvider){
        this.tarantulaContext.unregisterBackupProvider(backupProvider);
    }
    public BackupProvider backupProvider(){
        return this.tarantulaContext.backupProvider();
    }

}
