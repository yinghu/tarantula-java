package com.tarantula.platform;

import com.icodesoftware.*;
import com.icodesoftware.EventListener;
import com.icodesoftware.Module;
import com.icodesoftware.service.ServiceProvider;
import com.tarantula.platform.event.*;
import com.tarantula.platform.service.deployment.ApplicationContextProxy;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;



public class TarantulaApplicationContext implements ApplicationContext, EventListener, Connection.OnStateListener{

    private Descriptor _descriptor;
	private TarantulaApplication application;

    private TarantulaContext tarantulaContext;
    private String applicationId;

     private ConcurrentHashMap<Integer,RecoverableListener> rMap = new ConcurrentHashMap<>();

    private HashMap<String,Configuration> configurations;

    private boolean singleton;

    private boolean logEnabled;
    private TarantulaLogger log;

    private TokenValidator validator;

    private final boolean resetEnabled;

    public TarantulaApplicationContext(TarantulaContext tarantulaContext,Descriptor descriptor,TarantulaApplication application,HashMap<String,Configuration> configurations){
        this.tarantulaContext = tarantulaContext;
        this._descriptor = descriptor;
        this.application = application;
        this.configurations = configurations;
        this.resetEnabled = descriptor.resetEnabled();
    }
    public String typeId(){
        return this._descriptor.typeId();
    }
    public synchronized void onState(Connection onConnection){
        this.application.onState(onConnection);
    }

    public void onError(Event me,Exception ex){
        this.application.onError(me, ex);
    }
    public void actOnSingleton(Event me) throws Exception{
        this.application.callback(me,me.payload());
    }


    public void _setup() throws Exception{
        this.validator = this.tarantulaContext.tokenValidatorProvider().tokenValidator();
        //this.duration = _descriptor.runtimeDurationOnInstance();
        //this.timed = this.duration>0;
        this.logEnabled = _descriptor.logEnabled();
        if(logEnabled){
            this.log = this.tarantulaContext.logger(this.application.getClass());
        }

        this._setup( _descriptor.distributionKey(), _descriptor.singleton());
    }
    private void _setup(String applicationId,boolean singleton) throws Exception {
        this.applicationId = applicationId;
        this.singleton = singleton;
        if(singleton){
            this.application.setup(new ApplicationContextProxy(this));
        }
    }

    public Configuration configuration(String type){
       return this.configurations.get(type);
    }
    public List<Configuration> configuration(){
       return new ArrayList<>(this.configurations.values());
    }
    public ScheduledFuture<?> schedule(SchedulingTask task){
        return this.tarantulaContext.schedule(task);
    }
    public Presence presence(String systemId){ //always available on cluster scope if access is active
        return this.tarantulaContext.tokenValidatorProvider().presence(systemId);
    }
    public void absence(Session session){
        this.validator.offSession(session.systemId(),session.stub());
    }

    public Lobby lobby(String typeId){
        return this.tarantulaContext.lobby(typeId);
    }
    public List<Lobby> index(){
        return this.tarantulaContext.lobbyList();
    }

    public boolean onEvent(Event event) {
        if(event instanceof MapStoreSyncEvent){
            MapStoreSyncEvent msc = (MapStoreSyncEvent)event;
            RecoverableListener rc = this.rMap.get(msc.accessMode);
            if(rc!=null){
                rc.onUpdated(msc.stub(),msc.systemId,msc.index(),msc.payload());
            }
            else{
                this.application.onEvent(event);
            }
        }else{
            this.application.onEvent(event);
        }
        return true;
    }


    public Descriptor descriptor(){
        return this._descriptor;
    }
    public TokenValidator validator(){
        return this.validator;
    }
    public <T extends ServiceProvider> T serviceProvider(String name){
        return (T)this.tarantulaContext.serviceProvider(name);
    }

    public RecoverableListener registerRecoverableListener(RecoverableListener recoverableListener){
        return rMap.computeIfAbsent(recoverableListener.registryId(),(rid)-> recoverableListener);
    }
    public void unregisterRecoverableListener(int factoryId){
        this.rMap.remove(factoryId);
    }

    public DataStore dataStore(String name){
        return this.tarantulaContext.dataStore(name,this.tarantulaContext.partitionNumber());
    }

    public void log(String message,int level){
        if(this.logEnabled){
            switch (level){
                case OnLog.DEBUG:
                    this.log.debug(message);
                    break;
                case OnLog.INFO:
                    this.log.info(message);
                    break;
                case OnLog.WARN:
                    this.log.warn(message);
                    break;
            }
        }
    }
    public void log(String message,Exception error,int level){
        if(this.logEnabled){
            switch (level){
                case OnLog.WARN:
                    if(error!=null){
                        this.log.warn(message);
                    }
                    else{
                        this.log.warn(message,error);
                    }
                    break;
                case OnLog.ERROR:
                    this.log.error(message,error);
                    break;
            }
        }
    }
    public void onBucketReceiver(int bucket,int state){
        this.application.onBucket(bucket,state);
    }
    public void resource(String name, Module.OnResource onResource){
        this.tarantulaContext.deploymentService().resource(this.descriptor(),name,onResource);
    }
    public PostOffice postOffice(){
        return this.tarantulaContext.deploymentService().registerPostOffice();
    }

    public void clear(){
        this.application.clear();
        configurations.clear();
        rMap.clear();
    }
}
