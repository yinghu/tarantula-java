package com.tarantula.platform.module;

import com.tarantula.*;
import com.tarantula.platform.CompositeKey;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.TarantulaApplicationHeader;
import com.tarantula.platform.event.FastPlayEvent;
import com.tarantula.platform.event.InstancePlayEvent;
import com.tarantula.platform.service.DeploymentServiceProvider;
import com.tarantula.platform.util.LobbyContextSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ScheduledFuture;

/**
 * Updated by yinghu lu on 9/3/2019.
 */
public class LobbyApplication extends TarantulaApplicationHeader implements OnInstance.Listener,SchedulingTask{

    private ConcurrentHashMap<String, ConcurrentLinkedDeque<InstanceRegistry>> rQueue = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,InstanceRegistry> rMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<CompositeKey,OnInstance> oMap = new ConcurrentHashMap<>();

    private ConcurrentLinkedDeque<Event> eQueue = new ConcurrentLinkedDeque<>();


    private ConcurrentHashMap<String, Configuration> cMap = new ConcurrentHashMap<>();
    private int maxRetries = 3;
    private DeploymentServiceProvider deploymentServiceProvider;

    private ScheduledFuture timerSchedule;
    @Override
    public void callback(Session session, byte[] payload) throws Exception {
        OnAccess ex = this.builder.create().fromJson(new String(payload).trim(), OnAccess.class);
        if(session.action().equals("onLobby")){ //return available app list
            Lobby _lobby = this.context.lobby(ex.typeId());
            LobbyContext pc = new LobbyContext(session.action());
            List<Descriptor> gameList = new ArrayList();
            _lobby.entryList().forEach((d)->{
                if((!d.category().equals("lobby"))&&(!(d.category().equals("service")))){//excludes lobby and service category apps
                    gameList.add(d);
                }
            });
            pc.gameList = gameList;
            session.write(this.builder.create().toJson(pc).getBytes(),this.descriptor.responseLabel());
        }
        else if(session.action().equals("onList")){ //return available instance list
            int sz = Integer.parseInt(ex.property("size"));
            LobbyContext ic = new LobbyContext(session.action());
            ArrayList<InstanceRegistry> alist = new ArrayList<>();
            rMap.forEach((k,v)->{
                if(v.applicationId().equals(ex.applicationId())&&alist.size()<sz){
                    alist.add(v);
                }
            });
            ic.onList = alist;
            session.write(this.builder.create().toJson(ic).getBytes(),this.descriptor.responseLabel());
        }
        else{
            throw new RuntimeException("action ["+session.action()+"] not supported");
        }
    }

    @Override
    public boolean onEvent(Event event) {
        if(event instanceof FastPlayEvent){
            OnInstance joined = oMap.get(new CompositeKey(event.systemId(),event.applicationId()));//systemId + applicationId as the key
            if(joined!=null&&joined.joined()){
                //rejoin
                InstanceRegistry ij = rMap.get(joined.instanceId());
                if(ij!=null&&(!ij.disabled())){
                    event.instanceId(joined.instanceId());
                    event.accessMode(event.accessMode());
                    if(ij.onJoin(event)==InstanceRegistry.ALREADY_ON_INSTANCE){
                        refund(event.systemId(),event.applicationId());
                    }
                }
                else{
                    _retry(event);
                    rMap.remove(joined.instanceId());
                }
            }
            else{
                InstanceRegistry ir = rQueue.get(event.applicationId()).pollFirst();
                if(ir!=null&&(!ir.disabled())){
                    event.instanceId(ir.distributionKey());
                    event.accessMode(ir.accessMode());
                    int ret = ir.onJoin(event);
                    rQueue.get(event.applicationId()).addFirst(ir);
                    switch (ret){
                        case InstanceRegistry.INSTANCE_FULL://retry
                            eQueue.addLast(event);
                            break;
                        case InstanceRegistry.ALREADY_ON_INSTANCE://refund silently
                            refund(event.systemId(),ir.applicationId());
                            break;
                    }
                }
                else{
                    _retry(event);
                    if(ir!=null){
                        this.context.log("ins not removed->"+ir.disabled()+"/"+ir.distributionKey(),OnLog.WARN);
                    }
                }
            }
        }
        else if(event instanceof InstancePlayEvent){//instance join
            //context.log("listing play->"+event.applicationId(),OnLog.WARN);
            InstanceRegistry ir = rMap.get(event.instanceId());
            event.accessMode(ir.accessMode());
            int ret = ir.onJoin(event);
            switch (ret){
                case InstanceRegistry.INSTANCE_FULL://refund and send fully joined message to players
                    refund(event.systemId(),ir.applicationId());
                    ResponseHeader rh = new ResponseHeader("onPlay",false,Response.INSTANCE_FULL,"instance fully joined","error");
                    event.write(this.builder.create().toJson(rh).getBytes(),this.descriptor.responseLabel());
                    break;
                case InstanceRegistry.ALREADY_ON_INSTANCE://refund silently
                    refund(event.systemId(),ir.applicationId());
                    break;
            }
        }
        return true;
    }
    private void _retry(Event event){
        if(event.retries()<maxRetries){
            eQueue.addLast(event);//retry it
        }
        else{//failed after reaching max retries
            refund(event.systemId(),event.applicationId());
            ResponseHeader rh = new ResponseHeader("onPlay",false,Response.INSTANCE_FULL,"no available instance after retries ["+event.retries()+"]","error");
            event.write(this.builder.create().toJson(rh).getBytes(),this.descriptor.responseLabel());
        }
    }
    @Override
    public void onRegistry(InstanceRegistry instanceRegistry){
        //this.context.log("ins->"+instanceRegistry.disabled()+"/"+instanceRegistry.distributionKey(),OnLog.WARN);
        if(!instanceRegistry.disabled()){
            instanceRegistry.registerOnInstanceListener(this);
            rMap.put(instanceRegistry.distributionKey(),instanceRegistry);
            rQueue.computeIfAbsent(instanceRegistry.applicationId(),(k)->new ConcurrentLinkedDeque<>()).addLast(instanceRegistry);
        }
        else{
            rMap.remove(instanceRegistry.distributionKey());
            boolean removed = rQueue.get(instanceRegistry.applicationId()).remove(instanceRegistry);
            oMap.forEach((k,c)->{
                if(c.applicationId().equals(instanceRegistry.applicationId())){
                    oMap.remove(k);
                }
            });
            this.context.log("ins removed->"+instanceRegistry.disabled()+"/"+instanceRegistry.distributionKey()+"/"+removed,OnLog.WARN);
        }
    }
    @Override
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        this.builder.registerTypeAdapter(LobbyContext.class,new LobbyContextSerializer());
        this.deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        this.deploymentServiceProvider.registerInstanceRegistryListener(this);
        this.context.configuration().forEach((c)->{
            cMap.put(c.type(),c);
        });
        this.timerSchedule = this.context.schedule(this);
        this.context.log("Lobby application is started on ["+this.onLobby()+"]",OnLog.INFO);
    }
    public void onBucket(int bucket,int state) {
        this.context.log(bucket+"/"+state,OnLog.INFO);
    }
    @Override
    public void onUpdated(OnInstance onInstance) {
        if(onInstance.joined()){
            oMap.put(new CompositeKey(onInstance.systemId(),onInstance.applicationId()),onInstance);
        }
        else{
            oMap.remove(new CompositeKey(onInstance.systemId(),onInstance.applicationId()));
        }
    }

    @Override
    public boolean oneTime() {
        return false;
    }

    @Override
    public long initialDelay() {
        return 0;
    }

    @Override
    public long delay() {
        return 1000;
    }

    @Override
    public void run() {
        Event pending;
        do{
            pending = eQueue.pollFirst();
            if(pending!=null){
                pending.retries(pending.retries()+1);
                this.onEvent(pending);
            }
        }while (pending!=null);
    }
    @Override
    public void clear(){
        this.timerSchedule.cancel(true);
        this.rMap.clear();
        this.eQueue.clear();
        this.oMap.clear();
        this.cMap.clear();
        this.rQueue.clear();
        this.context.log("Lobby ["+this.onLobby()+"/"+this.descriptor.distributionKey()+"] closed",OnLog.WARN);
    }
}
