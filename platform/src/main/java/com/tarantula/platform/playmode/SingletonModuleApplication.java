package com.tarantula.platform.playmode;

import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.TarantulaApplicationHeader;
import com.tarantula.platform.event.FastPlayEvent;
import com.tarantula.platform.service.DeploymentServiceProvider;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Updated by yinghu lu on 4/14/2020.
 */
public class SingletonModuleApplication extends TarantulaApplicationHeader implements SchedulingTask {

    //private ConcurrentHashMap<String,Session> _onStream = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,ConcurrentHashMap<String,Session>> _onIndex = new ConcurrentHashMap<>();

    private long SERVER_PUSH_INTERVAL = 50;

    private Module module;
    private DeploymentServiceProvider serviceProvider;
    private long pendingTimer;

    @Override
    public void callback(Session session, byte[] payload) throws Exception {
        if(session.streaming()){
            this._onIndex.putIfAbsent(session.instanceId(),new ConcurrentHashMap<>()).put(session.systemId(),session);
        }
        else if(this.module.onRequest(session,payload,((uid,delta) ->{
            _onIndex.putIfAbsent(parseUid(uid),new ConcurrentHashMap<>()).forEach((k,v)->{
                v.write(delta,this.module.label()+"#"+uid);
            });
        }))){
            //clean up on leave
            //this.context.log("Session->"+session.systemId()+"//"+session.instanceId(),OnLog.INFO);
            Session rm = this._onIndex.get(session.instanceId()).remove(session.systemId());
            if(rm!=null){
                ResponseHeader resp = new ResponseHeader(session.action(),"close session");
                rm.write(this.builder.create().toJson(resp).getBytes(),module.label()+"#"+session.instanceId()+"?"+session.action(),true);
            }
        }
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        this.serviceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        module = this.serviceProvider.module(this.descriptor);
        pendingTimer = descriptor.timerOnModule();
        if(descriptor.timerOnModule()>0){
            this.context.schedule(this);
        }
        module.setup(context);
        this.context.log("Singleton Dynamic Module Started On ["+descriptor.moduleName()+"]", OnLog.INFO);
    }
    @Override
    public boolean oneTime() {
        return false;
    }

    @Override
    public long initialDelay() {
        return SERVER_PUSH_INTERVAL;
    }

    @Override
    public long delay() {
        return SERVER_PUSH_INTERVAL;
    }

    @Override
    public void run() {
        try{
            pendingTimer = pendingTimer-SERVER_PUSH_INTERVAL;
            if(pendingTimer<=0){
                this.module.onTimer(((uid,delta) -> {
                        if(delta==null){
                            _onIndex.remove(parseUid(uid)).forEach((k,v)->{
                                ResponseHeader resp = new ResponseHeader("onLeave","close session");
                                v.write(this.builder.create().toJson(resp).getBytes(),module.label()+"#"+uid,true);
                            });
                            return;
                        }
                        _onIndex.putIfAbsent(parseUid(uid),new ConcurrentHashMap<>()).forEach((k,v)->{
                            v.write(delta,this.module.label()+"#"+uid);
                        });
                    }
                ));
                pendingTimer = descriptor.timerOnModule();
            }
        }catch (Exception ex){
            //ignore it
        }
    }
    public boolean onEvent(Event event){
        try{
            if(event instanceof FastPlayEvent){
                this.module.onJoin(event,(uid,delta)->{
                    _onIndex.putIfAbsent(parseUid(uid),new ConcurrentHashMap<>()).forEach((k,v)->{
                        v.write(delta,this.module.label()+"#"+uid);
                    });
                });
            }
        }catch (Exception ex){
            //write error to client
            this.onError(event,ex);
        }
        return false;
    }
    private String parseUid(String uid){
        int ix = uid.indexOf('?');
        if(ix>0){
            return uid.substring(0,ix);
        }
        else{
            return uid;
        }
    }
}
