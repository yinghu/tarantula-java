package com.tarantula.platform.playmode;

import com.tarantula.*;
import com.tarantula.Module;

import com.tarantula.platform.TarantulaApplicationHeader;
import com.tarantula.platform.event.FastPlayEvent;
import com.tarantula.platform.service.DeploymentServiceProvider;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by yinghu lu on 7/31/2019.
 */
public class SingletonModuleApplication extends TarantulaApplicationHeader implements SchedulingTask {

    //private ConcurrentHashMap<String,Session> _onStream = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,CopyOnWriteArrayList<Session>> _onIndex = new ConcurrentHashMap<>();

    private long SERVER_PUSH_INTERVAL = 50;

    private Module module;
    private DeploymentServiceProvider serviceProvider;
    private long pendingTimer;

    @Override
    public void callback(Session session, byte[] payload) throws Exception {
        if(session.streaming()){
            this.context.log("Session->"+session.instanceId(),OnLog.INFO);
            this._onIndex.getOrDefault(session.instanceId(),new CopyOnWriteArrayList<>()).add(session);
        }
        else if(this.module.onRequest(session,payload,((uid,delta) ->{
            _onIndex.getOrDefault(parseUid(uid),new CopyOnWriteArrayList<>()).forEach((v)->{
                v.write(delta,this.module.label()+"#"+uid);
            });
        }))){
            //clean up on leave
            //this.context.log("Session->"+session.systemId(),OnLog.INFO);
            //Session rm = this._onStream.remove(session.systemId());
            //if(rm!=null){
                //ResponseHeader resp = new ResponseHeader(session.action(),"close session");
                //rm.write(this.builder.create().toJson(resp).getBytes(),module.label(),true);
            //}
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
                            _onIndex.remove(parseUid(uid));
                            return;
                        }
                        _onIndex.getOrDefault(parseUid(uid),new CopyOnWriteArrayList<>()).forEach((v)->{
                            this.context.log("Game id->"+uid,OnLog.INFO);
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
                    _onIndex.getOrDefault(parseUid(uid),new CopyOnWriteArrayList<>()).forEach((v)->{
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
