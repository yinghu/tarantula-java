package com.tarantula.platform.service.deployment;

import com.google.gson.GsonBuilder;
import com.icodesoftware.*;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.*;
import com.tarantula.platform.event.EventOnAction;
import com.tarantula.platform.event.OnDeployEvent;
import com.tarantula.platform.service.BucketReceiver;
import com.tarantula.platform.service.Instance;
import com.tarantula.platform.util.ResponseSerializer;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Updated by yinghu lu on 3/5/2019.
 */
public class InstanceManager implements Instance {

    private static TarantulaLogger log = JDKLogger.getLogger(InstanceManager.class);

    private int partition;
    private int state;
    private AtomicInteger instancesOnPartition;
    private ConcurrentHashMap<String,TarantulaApplicationContext> tMap = new ConcurrentHashMap<>();
    private ConcurrentLinkedDeque<TarantulaApplicationContext> pendingQueue = new ConcurrentLinkedDeque<>();
    private ApplicationManager applicationManager;

    private final long durationOnInstance;
    private GsonBuilder builder;


    public InstanceManager(int partition, ApplicationManager applicationManager){

        this.partition = partition;
        this.applicationManager = applicationManager;
        this.durationOnInstance = this.applicationManager.deploymentDescriptor.runtimeDurationOnInstance();
        this.instancesOnPartition = new AtomicInteger(0);

    }
    @Override
    public String routingKey(){
        return new StringBuffer(applicationManager.descriptor().distributionKey()).append(Recoverable.PATH_SEPARATOR).append(partition).toString();
    }
    @Override
    public String applicationId(){
        return this.applicationManager.descriptor().distributionKey();
    }
    @Override
    public int partition(){
        return this.partition;
    }
    @Override
    public void start() throws Exception {
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(ResponseHeader.class,new ResponseSerializer());
    }

    @Override
    public void shutdown() throws Exception {
        this.tMap.forEach((k,v)->{
            try{
                v.onLeaveAll();
                v.clear();
            }catch (Exception ex){
                ex.printStackTrace();
            }
        });
        this.tMap.clear();
        log.warn("Instance Manager ["+this.routingKey()+"] shut down on partition ["+partition+"]");
    }
    private TarantulaApplicationContext tarantulaApplicationContext(Event event){
        if(event.instanceId()==null){
            event.instanceId(event.source());//from internal app requests
        }
        return tMap.computeIfAbsent(event.instanceId(),(k)-> {
            TarantulaApplicationContext tc = this.applicationManager.launch(event);
            if(tc!=null){
                try{
                    tc._setup();//inject the app context proxy to decouple the TarantulaApplicationContext
                    tc.setupOnInstanceRegistry();//loading instance state
                    tc.onBucketReceiver(partition,state);
                    log.warn("Instance ["+tc._instance.distributionKey()+"] is running limited time mode ["+(durationOnInstance>0)+"]");
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
            return tc;
        });
    }
    @Override
    public boolean onEvent(Event event) {
        //check applicationId on event with deployment id
        TarantulaApplicationContext tac = tarantulaApplicationContext(event);
        if(tac==null){
            log.warn("Instance ["+event.instanceId()+"] not available");
            ResponseHeader rend = new ResponseHeader("onEnd","instance not available");
            rend.successful(false);
            rend.label("error");
            event.write(this.builder.create().toJson(rend).getBytes(),"error");
            return false;
        }
        try{
            if(event instanceof EventOnAction){
                if(this.applicationManager.checkAccessControl(event)){
                    tac.actOnInstance(event);
                }
                else{
                    throw new IllegalAccessException("Illegal access ->"+applicationManager.deploymentDescriptor.name());
                }
            }
            else{
                tac.onEvent(event);
            }
            tac.onTouch(event);
        }catch (Exception ex){
            tac.onError(event,ex);
        }
        return false;
    }

    @Override
    public void onBucketReceiver(int state,BucketReceiver bucketReceiver) {
        this.state = state;
        if(state==BucketReceiver.OPEN){
            this.applicationManager.configure(this);
            if(instancesOnPartition.get()==0){
                this.applicationManager.launch(this);
            }
        }
        else{
            this.tMap.forEach((k,t)->{
                t.onBucketReceiver(bucketReceiver.partition(),state);
            });
            this.tMap.clear();
        }
    }
    public void onPartition(String instanceId){
        TarantulaApplicationContext tc = this.tarantulaApplicationContext(new OnDeployEvent(instanceId));
        tc._instance.application = this;
        instancesOnPartition.incrementAndGet();
    }
    public int onJoin(Event event, List<OnInstance.Listener> onInstanceListener){
        TarantulaApplicationContext tcx = this.tarantulaApplicationContext(event);
        if(tcx==null){
            log.warn("Instance ["+event.instanceId()+"] not available on join and try again");
            return InstanceRegistry.INSTANCE_FULL;
        }
        OnInstance onInstance = tcx.poll(event);//pre-join
        int ret = InstanceRegistry.INSTANCE_FULL;
        if(onInstance!=null){//check applicationId on event with deployment id
            ret = onInstance.joined()?InstanceRegistry.ALREADY_ON_INSTANCE:InstanceRegistry.ON_INSTANCE;
            if(tcx.initializeOnInstance(event,onInstance)){
                onInstanceListener.forEach((l)->{
                    l.onUpdated(new OnInstanceTrack(event.systemId(),event.stub(),applicationManager.deploymentDescriptor.distributionKey(),onInstance.instanceId(),true));
                });
            }
        }
        else{
            if(event.retries()>3&&instancesOnPartition.get()<this.applicationManager.deploymentDescriptor.maxInstancesPerPartition()){//check max instances per partition
                this.applicationManager.launch(this);
            }
        }
        return ret;
    }
    public void onCheck(){
        tMap.forEach((c,t)->{
            if(t.onCheck()){//instance kick off
                try{
                    tMap.remove(c);
                    t.onLeaveAll();
                    t.clear();
                    applicationManager.unload(c);
                    instancesOnPartition.decrementAndGet();
                    if(instancesOnPartition.get()<this.applicationManager.deploymentDescriptor.maxInstancesPerPartition()){
                        this.applicationManager.launch(this);
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
    }
}
