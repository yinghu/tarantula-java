package com.tarantula.platform;

import com.tarantula.*;
import com.tarantula.EventListener;
import com.tarantula.Module;
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


public class TarantulaApplicationContext implements ApplicationContext, EventListener{

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

    private DeltaStatistics onStatistics;
    private boolean logEnabled;
    private TarantulaLogger logger;

    private TokenValidator validator;

    private AtomicBoolean started = new AtomicBoolean(false);
    public long duration;
    private boolean timed;
    private final boolean resetEnabled;
    private final String dataStore;
    public TarantulaApplicationContext(TarantulaContext tarantulaContext,Descriptor descriptor,TarantulaApplication application,InstanceIndex index,HashMap<String,Configuration> configurations){
        this.tarantulaContext = tarantulaContext;
        this._descriptor = descriptor;
        this.application = application;
        this._instance = index; //null on singleton instance
        this.configurations = configurations;
        this.resetEnabled = descriptor.resetEnabled();
        this.dataStore = descriptor.typeId();
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
                oi.tournamentEnabled(_instance.tournamentEnabled);
                oi.idle(true);
            }
        }
        return oi;
    }

    public void initializeOnApplication(Event me,OnInstance onApplication){//operate on on instance node
        try{
            if(this.validator().validTicket(me.systemId(),me.stub(),me.ticket())){
                onInstances.put(me.systemId(),onApplication);
                _instance.entryCost(this.descriptor().entryCost());
                if(onApplication.tournamentEnabled()){
                }
                me.balance(onApplication.balance());
                if((!onApplication.initialized())){//without calling auditor
                    onApplication.initialized(true);
                    this.application.initialize(me);//initialize on application
                }
                else{
                    this.application.initialize(me);
                }
                onApplication.joined(me.joined());
                onApplication.update();
            }
            else{
                throw new RuntimeException("session expired ["+me.ticket()+"]");
            }
        }catch (Exception ex){
            ex.printStackTrace();
            waitingList.offer(onApplication);
            this.application.onError(me, ex);
        }
    }
    public void onRequestCallback(Event me){
        if((!singleton)){
            this.actOnApplication(me);
        }
        else{
            this.actOnSingleton(me);
        }
    }
    public void onError(Event me,Exception ex){
        this.application.onError(me, ex);
    }
    private void actOnSingleton(Event me){
        try{
            this.application.callback(me,me.payload());
        }catch (Exception ex){
            this.application.onError(me, ex);
        }
    }

	private void actOnApplication(Event me){//operate on instance ID node
        try{
            if(this.validator().validTicket(me.systemId(),me.stub(),me.ticket())){
                OnInstance onApplication = this.onInstances.get(me.systemId());
                if(onApplication==null){
                    throw new IllegalArgumentException("access rejected from ["+me.systemId()+"]");//have to registered on application
                }
                me.balance(onApplication.balance());
                if(onApplication.initialized()){//callback per session without validation
                    me.joined(onApplication.joined());
                    this.application.callback(me,me.payload());
                }
                else{
                    throw new RuntimeException("session not initialized");
                }
            }else{
                throw new RuntimeException("session expired ["+me.ticket()+"/"+me.systemId()+"/"+me.stub()+"/"+me.action()+"]");
            }
        }
        catch (Exception ex){
            this.application.onError(me, ex);
        }
		
	}

    public void _setup() throws Exception{
        this.validator = this.tarantulaContext.tokenValidatorProvider.tokenValidator();
        this.duration = _descriptor.runtimeDurationOnInstance();
        this.timed = this.duration>0;
        this.logEnabled = _descriptor.logEnabled();
        if(logEnabled){
            this.logger = this.tarantulaContext.logger(this.application.getClass());
        }
        if(_descriptor.singleton()){ //per header per singleton
            DataStore ds = this.tarantulaContext.masterDataStore();
            this.onStatistics = new DeltaStatistics(); //LOCAL NODE ONLY
            this.onStatistics.vertex(SystemUtil.toString(new String[]{this.onStatistics.vertex(),ds.node()}));
            this.onStatistics.distributionKey(_descriptor.distributionKey());
            this.onStatistics.leaderBoardHeader(_descriptor.leaderBoardHeader());
            //this.onStatistics.distributable(true);
            if(this.tarantulaContext.tarantulaCluster.load(this.onStatistics)||ds.load(this.onStatistics)){
                ds.createIfAbsent(this.onStatistics,false);
                ds.list(new StatisticsEntryQuery(this.onStatistics.key().asString()),(e)->{
                    this.onStatistics.entry(e);
                    ds.createIfAbsent(e,false);
                    return true;
                });
                this.onStatistics.dataStore(this.dataStore());
            }
            else{
                ds.create(this.onStatistics);
                this.onStatistics.dataStore(this.dataStore());
            }
            if(this.tarantulaContext.tarantulaCluster.load(this.onStatistics)){
                //logger.warn("Node only statistics ->"+this.onStatistics.key().asString());
            }
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
                    if(a.initialized()){
                        _instance.count(1);
                        a.idle(true);
                        this.onInstances.put(a.systemId(),a);
                        _instance.onInstanceListener.onUpdated(new OnInstanceTrack(a.systemId(),a.stub(),this.applicationId,_instance.distributionKey(),true));
                    }
                    else{
                        this.waitingList.offer(a);
                    }
                    a.dataStore(this.tarantulaContext.masterDataStore());
                    a.owner(this._instance.distributionKey());
                });
                House house = this.tarantulaContext.query(_instance.distributionKey(),new HouseTrack());
                _instance.house(house);
                _instance.house().dataStore(this.tarantulaContext.masterDataStore());
                DeltaStatistics statistics = this.tarantulaContext.query(_instance.distributionKey(),new DeltaStatistics());
                List<StatisticsEntry> elist = this.tarantulaContext.query(new String[]{statistics.key().asString()},new StatisticsEntryQuery(statistics.key().asString()));
                elist.forEach((e)->statistics.entry(e));
                _instance.statistics(statistics);
                _instance.statistics().dataStore(this.tarantulaContext.masterDataStore());
                this.application.setup(new ApplicationContextProxy(this));
            }catch (Exception ex){
                throw new RuntimeException(ex);
            }
        }
    }
    public void releaseOnInstanceRegistry(){//call on bucket closed
        this._instance.house().update();
        this._instance.statistics().update();
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
        return this.tarantulaContext.tokenValidatorProvider.presence(systemId);
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
        try{
            if(event instanceof MapStoreSyncEvent){
                MapStoreSyncEvent msc = (MapStoreSyncEvent)event;
                Metadata md = msc.metadata;
                RecoverableListener rc = this.rMap.get(md.factoryId());
                if(rc!=null){
                    rc.onUpdated(md,msc.key,msc.payload());
                }
                else{
                    this.application.onEvent(event);
                }
            }else{
                this.application.onEvent(event);
            }
        }catch (Exception ex){
            this.application.onError(event,ex);
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
        on.initialized(false);
        on.joined(false);
        on.update();
        if((!on.tournamentEnabled())&&on.balance()>0){//move remaining balance to presence on non-tournament mode
            this.postOffice().onTag(Presence.LOBBY_TAG).send(systemId,new OnBalanceTrack(systemId,on.balance()));
        }
        this.waitingList.offer(on);
        return 1;
    }

    public InstanceRegistry onRegistry(){
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
    public Statistics statistics(){
        return this.singleton?this.onStatistics:this._instance.statistics();
    }

    public DataStore dataStore(String name){
        if(resetEnabled){
            return this.tarantulaContext.dataStore(dataStore);
        }
        return this.tarantulaContext.dataStore(name,this.tarantulaContext.partitionNumber());
    }
    private DataStore dataStore(){
        return this.tarantulaContext.masterDataStore();
    }

    public void log(String message,int level){
        if(this.logEnabled){
            switch (level){
                case OnLog.DEBUG:
                    this.logger.debug(message);
                    break;
                case OnLog.INFO:
                    this.logger.info(message);
                    break;
                case OnLog.WARN:
                    this.logger.warn(message);
                    break;
            }
        }
    }
    public void log(String message,Exception error,int level){
        if(this.logEnabled){
            switch (level){
                case OnLog.WARN:
                    if(error!=null){
                        this.logger.warn(message);
                    }
                    else{
                        this.logger.warn(message,error);
                    }
                    break;
                case OnLog.ERROR:
                    this.logger.error(message,error);
                    break;
            }
        }
    }
    public void onBucketReceiver(int bucket,int state){
        if(!this.singleton){
            if(state== BucketReceiver.CLOSE){
                this.releaseOnInstanceRegistry();
            }
            else if(state==BucketReceiver.SHUT_DOWN){
                this.releaseOnInstanceRegistry();
            }
        }
        else{
            this.onStatistics.distributable(state==BucketReceiver.CLOSE);
            this.onStatistics.update();
        }
        this.application.onBucket(bucket,state);
    }
    public void resource(String name,Module.OnResource onResource){
        this.tarantulaContext.deploymentService().resource(this.descriptor(),name,onResource);
    }

    public PostOffice postOffice(){
        return this.tarantulaContext.deploymentServiceProvider.registerPostOffice();
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
