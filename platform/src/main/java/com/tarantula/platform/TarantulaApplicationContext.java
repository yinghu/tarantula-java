package com.tarantula.platform;

import com.icodesoftware.*;
import com.icodesoftware.EventListener;
import com.icodesoftware.Module;
import com.icodesoftware.util.BufferProxy;
import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.ServiceProvider;
import com.tarantula.platform.event.*;
import com.tarantula.platform.service.deployment.ApplicationContextProxy;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;



public class TarantulaApplicationContext implements ApplicationContext, EventListener{

    private Descriptor _descriptor;
	private TarantulaApplication application;

    private TarantulaContext tarantulaContext;

    private ConcurrentHashMap<Integer,RecoverableListener> rMap = new ConcurrentHashMap<>();

    private MetricsListener metricsListener = (k,v)->{};

    private boolean logEnabled;
    private TarantulaLogger log;

    private TokenValidator validator;

    public TarantulaApplicationContext(TarantulaContext tarantulaContext,Descriptor descriptor,TarantulaApplication application){
        this.tarantulaContext = tarantulaContext;
        this._descriptor = descriptor;
        this.application = application;
    }
    public void onError(Event me,Exception ex){
        this.application.onError(me, ex);
    }
    public void actOnSingleton(Event me) throws Exception{
        this.application.callback(me,me.payload());
    }


    public void _setup() throws Exception{
        this.validator = this.tarantulaContext.tokenValidatorProvider().tokenValidator();
        this.logEnabled = _descriptor.logEnabled();
        if(logEnabled){
            this.log = _descriptor.moduleName()!=null?this.tarantulaContext.logger(_descriptor.moduleName()):this.tarantulaContext.logger(this.application.getClass());
        }
        this.application.setup(new ApplicationContextProxy(this));
    }

    public Configuration configuration(String name){
        return this.tarantulaContext.configuration(_descriptor.typeId()+"-"+name+"-settings");//this.configurations.get(name);
    }

    public ScheduledFuture<?> schedule(SchedulingTask task){
        return this.tarantulaContext.schedule(task);
    }
    public Presence presence(Session session){ //always available on cluster scope if access is active
        return this.tarantulaContext.tokenValidatorProvider().presence(session);
    }
    public void absence(Session session){
        this.validator.offSession(session.distributionId(),session.stub());
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
            RecoverableListener rc = this.rMap.get(msc.factoryId);
            if(rc!=null){
                Recoverable r = rc.create(msc.classId);
                r.readKey(BufferProxy.buffer(msc.key));
                r.read(BufferProxy.buffer(msc.value));
                rc.onUpdated(r);
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
        String _dname = this._descriptor.typeId().replaceAll("-","_")+"_atc_"+name;
        return this.tarantulaContext.dataStore(Distributable.DATA_SCOPE,_dname);
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
        return this.tarantulaContext.postOffice();
    }

    public void clear(){
        this.application.clear();
        rMap.clear();
    }

    public ClusterProvider clusterProvider(){
        return this.tarantulaContext.clusterProvider();
    }


    public void onMetrics(String category,double delta){
        metricsListener.onUpdated(category,delta);
    }
    public ClusterProvider.Node node(){
        return tarantulaContext.node();
    }

    public Transaction transaction(){
        return this.tarantulaContext.deploymentDataStoreProvider.transaction(Distributable.DATA_SCOPE);
    }

    public void registerMetricsListener(MetricsListener metricsListener){
        if(metricsListener==null) return;
        this.metricsListener = metricsListener;
    }
}
