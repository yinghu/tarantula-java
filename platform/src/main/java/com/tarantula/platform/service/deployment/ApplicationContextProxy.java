package com.tarantula.platform.service.deployment;

import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.protocol.UDPEndpointServiceProvider;
import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.Metrics;
import com.icodesoftware.service.ServiceProvider;
import com.tarantula.platform.TarantulaApplicationContext;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;

public class ApplicationContextProxy implements ApplicationContext {
    private TarantulaApplicationContext tarantulaApplicationContext;
    public ApplicationContextProxy(TarantulaApplicationContext tarantulaApplicationContext){
        this.tarantulaApplicationContext = tarantulaApplicationContext;
    }
    @Override
    public Lobby lobby(String typeId) {
        return this.tarantulaApplicationContext.lobby(typeId);
    }

    @Override
    public List<Lobby> index() {
        return this.tarantulaApplicationContext.index();
    }

    @Override
    public Presence presence(Session session) {
        return this.tarantulaApplicationContext.presence(session);
    }

    @Override
    public void absence(Session session) {
        this.tarantulaApplicationContext.absence(session);
    }

    @Override
    public ScheduledFuture<?> schedule(SchedulingTask task) {
        return this.tarantulaApplicationContext.schedule(task);
    }


    @Override
    public Configuration configuration(String type) {
        return this.tarantulaApplicationContext.configuration(type);
    }

    @Override
    public TokenValidator validator() {
        return this.tarantulaApplicationContext.validator();
    }

    @Override
    public Descriptor descriptor() {
        return this.tarantulaApplicationContext.descriptor();
    }


    @Override
    public DataStore dataStore(String name) {
        return this.tarantulaApplicationContext.dataStore(name);
    }

    @Override
    public RecoverableListener registerRecoverableListener(RecoverableListener recoverableListener) {
        return this.tarantulaApplicationContext.registerRecoverableListener(recoverableListener);
    }
    public void unregisterRecoverableListener(int factoryId){
        this.tarantulaApplicationContext.unregisterRecoverableListener(factoryId);
    }
    @Override
    public void log(String message, int level) {
        this.tarantulaApplicationContext.log(message,level);
    }

    @Override
    public void log(String message, Exception error, int level) {
        this.tarantulaApplicationContext.log(message,error,level);
    }

    @Override
    public <T extends ServiceProvider> T serviceProvider(String name) {
        return this.tarantulaApplicationContext.serviceProvider(name);
    }

    @Override
    public void resource(String name, Module.OnResource onResource) {
        this.tarantulaApplicationContext.resource(name,onResource);
    }

    @Override
    public PostOffice postOffice() {
        return this.tarantulaApplicationContext.postOffice();
    }


    public ClusterProvider clusterProvider(){
        return this.tarantulaApplicationContext.clusterProvider();
    }

    public void onMetrics(String category,double delta){
        this.tarantulaApplicationContext.onMetrics(category,delta);
    }
    public ClusterProvider.Node node(){
        return this.tarantulaApplicationContext.node();
    }

    @Override
    public Transaction transaction() {
        return this.tarantulaApplicationContext.transaction();
    }

    @Override
    public Transaction transaction(int scope) {
        return tarantulaApplicationContext.transaction(scope);
    }

    @Override
    public Transaction.LogManager logManager() {
        return null;
    }

    public void execute(Runnable runnable){}

    public <T extends Object> T execute(Callable<T> callable){
        return null;
    }
}
