package com.tarantula.platform;

import com.icodesoftware.*;
import com.icodesoftware.EventListener;
import com.icodesoftware.Module;
import com.icodesoftware.service.ServiceProvider;
import com.tarantula.platform.event.*;
import com.tarantula.platform.service.Application;
import com.tarantula.platform.service.BucketReceiver;
import com.tarantula.platform.service.deployment.ApplicationContextProxy;
import com.tarantula.platform.util.SystemUtil;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;


public class TarantulaApplicationContext implements ApplicationContext, EventListener, Connection.Listener{

    private Descriptor _descriptor;
	private TarantulaApplication application;

    private TarantulaContext tarantulaContext;
    private String applicationId;

    private ConcurrentHashMap<String,OnInstance> onInstances = new ConcurrentHashMap();  //instance Id node cache
    private ConcurrentLinkedQueue<OnInstance> waitingList = new ConcurrentLinkedQueue<>(); //reuse on instance
    private ConcurrentHashMap<Integer,RecoverableListener> rMap = new ConcurrentHashMap<>();

    private HashMap<String,Configuration> configurations;

    private Session.TimeoutListener sessionTimeoutListener;

    private boolean singleton;

    public InstanceIndex _instance;

    private boolean logEnabled;
    private TarantulaLogger log;

    private TokenValidator validator;

    private AtomicBoolean started = new AtomicBoolean(false);
    public long duration;
    private boolean timed;
    private final boolean resetEnabled;

    public TarantulaApplicationContext(TarantulaContext tarantulaContext,Descriptor descriptor,TarantulaApplication application,InstanceIndex index,HashMap<String,Configuration> configurations){
        this.tarantulaContext = tarantulaContext;
        this._descriptor = descriptor;
        this.application = application;
        this._instance = index; //null on singleton instance
        this.configurations = configurations;
        this.resetEnabled = descriptor.resetEnabled();
    }
    public String typeId(){
        return this._descriptor.typeId();
    }
    public synchronized void onState(Connection onConnection){
        this.application.onState(onConnection);
    }
    public OnInstance poll(Event event){
        //load joined on instance if already existed
        OnInstance oi = onInstances.get(event.systemId());
        if(oi==null){
            oi = this.waitingList.poll();
            if(oi!=null){
                oi.systemId(event.systemId());
                oi.stub(event.stub());
                oi.routingNumber(event.routingNumber());
                oi.reset(this.application.descriptor().entryCost());
                oi.accessMode(event.accessMode());
                oi.timestamp(SystemUtil.toUTCMilliseconds(LocalDateTime.now()));
                oi.idle(true);
            }
        }
        return oi;
    }

    public boolean initializeOnInstance(Event me,OnInstance onApplication){//operate on on instance node
        boolean suc = false;
        try{
            onInstances.put(me.systemId(),onApplication);
            _instance.entryCost(this.descriptor().entryCost());
            me.balance(onApplication.balance());
            this.application.initialize(me);
            onApplication.joined(me.joined());
            onApplication.update();
            suc = true;
        }catch (Exception ex){
            this.log("error on initializeOnInstance",ex,OnLog.ERROR);
            waitingList.offer(onApplication);
            onApplication.joined(false);
            onApplication.update();
            this.application.onError(me, ex);
        }
        return suc;
    }
    public void onError(Event me,Exception ex){
        this.application.onError(me, ex);
    }
    public void actOnSingleton(Event me) throws Exception{
        this.application.callback(me,me.payload());
    }

	public void actOnInstance(Event me) throws Exception{//operate on instance ID node
        OnInstance onApplication = this.onInstances.get(me.systemId());
        if(onApplication==null){
            throw new IllegalArgumentException("access rejected from ["+me.systemId()+"]");//have to registered on application
        }
        me.balance(onApplication.balance());
        if(onApplication.joined()){//callback per session without validation
            this.application.callback(me,me.payload());
        }
        else{
            throw new RuntimeException("session not initialized");
        }
	}

    public void _setup() throws Exception{
        this.validator = this.tarantulaContext.tokenValidatorProvider().tokenValidator();
        this.duration = _descriptor.runtimeDurationOnInstance();
        this.timed = this.duration>0;
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
        else{
            if(this.application instanceof Session.TimeoutListener){
                if(this.application.descriptor().maxIdlesOnInstance()<=0){
                    this.application.descriptor().maxIdlesOnInstance(this.tarantulaContext.maxIdlesOnInstance);//use default count
                }
                this.sessionTimeoutListener=((Session.TimeoutListener)this.application);
            }
        }
    }
    public void setupOnInstanceRegistry(){
        if(!started.getAndSet(true)){
            try{
                List<OnInstance> olist = this.tarantulaContext.query(new String[]{this._instance.distributionKey()},new OnInstanceQuery(this._instance.distributionKey()));
                olist.forEach((a)->{
                    a.instanceId(this._instance.distributionKey());
                    if(a.joined()){
                        _instance.count(1);
                        a.idle(true);
                        this.onInstances.put(a.systemId(),a);
                        _instance.onInstanceListener.forEach((l)->{
                            l.onUpdated(new OnInstanceTrack(a.systemId(),a.stub(),this.applicationId,_instance.distributionKey(),true));
                        });
                    }
                    else{
                        this.waitingList.offer(a);
                    }
                    a.dataStore(this.tarantulaContext.masterDataStore());
                    a.owner(this._instance.distributionKey());
                });

                 this.application.setup(new ApplicationContextProxy(this));
            }catch (Exception ex){
                throw new RuntimeException(ex);
            }
        }
    }
    public void releaseOnInstanceRegistry(){//call on bucket closed
        onInstances.forEach((k,v)->{
            v.update();
        });
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
                rc.onUpdated(msc.stub(),msc.index,msc.payload());
            }
            else{
                this.application.onEvent(event);
            }
        }else{
            this.application.onEvent(event);
        }
        return true;
    }
    public int onLeave(Session session){
        return this._onLeave(session.systemId());
    }
    public void onLeaveAll(){
        this.onInstances.forEach((String s,OnInstance o)->_onLeave(o.systemId()));
    }
    private int _onLeave(String systemId){
        OnInstance on = this.onInstances.remove(systemId);
        if(on==null){
            return 0;
        }
        on.joined(false);
        on.update();
        if(on.balance()>0){//move remaining balance to presence on non-tournament mode
            this.postOffice().onTag(Presence.LOBBY_TAG).send(systemId,new OnBalanceTrack(systemId,on.balance()));
        }
        this.waitingList.offer(on);
        return 1;
    }

    public InstanceRegistry onRegistry(){
        if(_instance==null){
            throw new UnsupportedOperationException("no instance associated with a singleton");
        }
        return this._instance;
    }

    public OnInstance onInstance(String systemId){
        return this.onInstances.get(systemId);
    }
    public List<OnInstance> onInstance(){
        ArrayList<OnInstance> alist = new ArrayList();
        this.onInstances.forEach((String s,OnInstance o)->alist.add(o));
        return alist;
    }

    public String instanceId(){
        return this._instance.distributionKey();
    }

    private int maxIdle(){
        return (this.sessionTimeoutListener!=null)?this.application.descriptor().maxIdlesOnInstance():0;
    }
    public void onIdle(Session session){
        if(sessionTimeoutListener!=null){
            this.sessionTimeoutListener.onIdle(session);
        }
    }
    public void onTimeout(Session session) {
        if(this.sessionTimeoutListener!=null){
            this.sessionTimeoutListener.onTimeout(session);
        }
    }

    public Descriptor descriptor(String applicationId){
        Application app = this.tarantulaContext.applicationManager(applicationId);
        return app!=null?app.descriptor():null;
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
        if(!this.singleton){
            if(state== BucketReceiver.CLOSE || state == BucketReceiver.SHUT_DOWN){
                this.releaseOnInstanceRegistry();
                this.application.clear();
            }
        }

        this.application.onBucket(bucket,state);
    }
    public void resource(String name, Module.OnResource onResource){
        this.tarantulaContext.deploymentService().resource(this.descriptor(),name,onResource);
    }

    public PostOffice postOffice(){
        return this.tarantulaContext.deploymentService().registerPostOffice();
    }
    public void onTouch(Event event){
        try{
            this.onInstance(event.systemId()).idle(true);
        }catch (Exception ex){
            //ignore
        }
    }
    private void _check(OnInstance oi){
        try{
            int ic = oi.idle(false);
            if(ic==0){
                return;
            }
            else if(ic>0&&ic<this.maxIdle()){
                //call idle
                this.onIdle(new TimeoutEvent(oi.systemId(),oi.stub(),oi.routingNumber()));
            }
            else{
                //call timeout
                this.onTimeout(new TimeoutEvent(oi.systemId(),oi.stub(),oi.routingNumber()));
            }
        }catch (Exception ex){
            //ignore
        }
    }
    public boolean onCheck(){
        if(this.maxIdle()>0){
            onInstances.forEach((k,o)->{
                _check(o);
            });
        }
        if(!timed){
            return false;
        }
        return (duration -=Application.DELTA)<=0;
    }
    public void clear(){
        this.application.clear();
        onInstances.clear();
        waitingList.clear();
        configurations.clear();
        rMap.clear();
    }
}
