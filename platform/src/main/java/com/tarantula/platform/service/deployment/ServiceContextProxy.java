package com.tarantula.platform.service.deployment;

import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.tarantula.*;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.service.*;

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
    public EventService eventService(int scope) {
        return this.tarantulaContext.eventService(scope);
    }

    @Override
    public ClusterProvider clusterProvider(int scope) {
        return this.tarantulaContext.clusterProvider(scope);
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
    @Override
    public Connection endpoint(){
        return this.tarantulaContext.endpoint();
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
}
