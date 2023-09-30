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
    public DataStore dataStore(int scope,String name) {
        return this.tarantulaContext.dataStore(scope,name);
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
    public OnPartition[] partitions() {
        return this.tarantulaContext.partitions();
    }

    public Transaction transaction(int scope){
        return tarantulaContext.transaction(scope);
    }





    public ClusterProvider.Node node(){
        return this.tarantulaContext.node();
    }

    @Override
    public <T extends Recoverable> RecoverableRegistry<T> recoverableRegistry(int registryId) {
        return this.tarantulaContext.recoverableRegistry(registryId);
    }

    @Override
    public void recoverableRegistry(RecoverableListener recoverableListener) {
        this.tarantulaContext.recoverableRegistry(recoverableListener);
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

    public HttpClientProvider httpClientProvider(){
        return this.tarantulaContext.httpClientProvider();
    }
    public PostOffice postOffice(){
        return this.tarantulaContext.postOffice();
    }

    public void log(String message,int level){
        this.tarantulaContext.log(message,level);

    }
    public void log(String message,Exception error,int level){
        this.tarantulaContext.log(message,error,level);
    }

    public ServiceEventLogger serviceEventLogger(){
        return this.tarantulaContext.serviceEventLogger();
    }
    public KeyIndexService keyIndexService(){
        return this.tarantulaContext.keyIndexService();
    }

    @Override
    public long distributionId() {
        return this.tarantulaContext.distributionId();
    }
}
