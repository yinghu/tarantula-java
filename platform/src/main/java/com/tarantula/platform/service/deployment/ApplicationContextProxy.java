package com.tarantula.platform.service.deployment;

import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.platform.TarantulaApplicationContext;
import com.tarantula.platform.service.ServiceProvider;

import java.util.ArrayList;
import java.util.List;
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
    public Presence presence(String systemId) {
        return this.tarantulaApplicationContext.presence(systemId);
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
    public InstanceRegistry onRegistry() {
        return this.tarantulaApplicationContext.onRegistry();
    }

    @Override
    public Configuration configuration(String type) {
        return this.tarantulaApplicationContext.configuration(type);
    }

    @Override
    public List<Configuration> configuration() {
        return this.tarantulaApplicationContext.configuration();
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
    public Descriptor descriptor(String applicationId) {
        return this.tarantulaApplicationContext.descriptor(applicationId);
    }

    @Override
    public Statistics statistics() {
        return this.tarantulaApplicationContext.statistics();
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

    public OnInstance onInstance(String systemId){
        return this.tarantulaApplicationContext.onInstance(systemId);
    }
    public List<OnInstance> onInstance(){
        return this.tarantulaApplicationContext.onInstance();
    }

}
