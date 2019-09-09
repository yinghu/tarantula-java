package com.tarantula.platform.playmode;

import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.platform.SessionIdle;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.TarantulaApplicationHeader;
import com.tarantula.platform.service.DeploymentServiceProvider;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * Created by yinghu lu on 7/20/2019.
 */
public class DynamicModuleApplication extends TarantulaApplicationHeader implements Session.TimeoutListener, SchedulingTask {

    private ConcurrentHashMap<String,Session> _onStream = new ConcurrentHashMap<>();

    private long SERVER_PUSH_INTERVAL = 50;

    private Module module;
    private DeploymentServiceProvider serviceProvider;
    private long pendingTimer;

    private ScheduledFuture timerSchedule;

    @Override
    public void initialize(Session session) throws Exception {
        session.joined(true);
        module.onJoin(session,onConnection);
    }
    @Override
    public void callback(Session session, byte[] payload) throws Exception {
        if(session.streaming()){
            this._onStream.put(session.systemId(),session);
        }
        if(this.module.onRequest(session,payload,((systemId, delta) -> {

            Session stream;
            if(systemId!=null&&(stream =this._onStream.get(systemId))!=null){
                stream.write(delta,module.label());
            }else{//broadcasting to all streaming session
                this._onStream.forEach((k,v)->{
                    v.write(delta,this.module.label());
                });
            }
            //if(onConnection!=null){
                //this.context.postOffice().onConnection(onConnection.serverId()).send(this.module.label(),delta);
            //}
            //server push
        }))){
            //clean up on leave
            this.onTimeout(session);
        }
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        this.serviceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        try{
            module = this.serviceProvider.module(this.descriptor);
            this.pendingTimer = descriptor.timerOnModule();
            if(descriptor.timerOnModule()>0){
                this.timerSchedule = this.context.schedule(this);
            }
            module.setup(context);
            this.context.log("Dynamic Module Started On ["+descriptor.moduleName()+"]", OnLog.INFO);
        }
        catch (Exception ex){
            //never throw out to kill others
            this.context.log("Failed on module startup on ["+descriptor.moduleName()+"]",ex,OnLog.ERROR);
        }
    }

    @Override
    public void onTimeout(Session session) {
        if(!this.descriptor.singleton()){
            this.context.onRegistry().onLeave(session);
        }
        this._onStream.remove(session.systemId());
        //this.context.log("Timeout->"+session.systemId(),OnLog.INFO);
    }

    @Override
    public void onIdle(Session session){
        SessionIdle sessionIdle = new SessionIdle(module.label(),session.systemId(),session.stub());
        Session pending = _onStream.get(session.systemId());
        if(pending!=null){
            pending.write(this.builder.create().toJson(sessionIdle).getBytes(),module.label());
        }
        //this.context.log("Idle->"+session.systemId(),OnLog.INFO);
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
                this.module.onTimer(((systemId, delta) ->
                    _onStream.forEach((k,v)->
                        v.write(delta,module.label())
                    )
                ));
                pendingTimer = descriptor.timerOnModule();//reset
            }
        }catch (Exception ex){
            //ignore it
        }
    }
    @Override
    public void clear(){
        ResponseHeader rend = new ResponseHeader("onEnd","instance ended");
        rend.label("error");
        byte[] resp = this.builder.create().toJson(rend).getBytes();
        _onStream.forEach((c,s)->{
            s.write(resp,module.label(),true);
        });
        _onStream.clear();
        this.module.clear();
        if(timerSchedule!=null){
            timerSchedule.cancel(true);
        }
        this.context.log("Instance ["+descriptor.moduleName()+"/"+this.context.onRegistry().distributionKey()+"] closed",OnLog.WARN);
    }
    @Override
    public void onError(Session session, Exception ex) {
        this.context.log(session.toString(),ex,OnLog.ERROR);
        String msg = ex.getMessage()!=null?ex.getMessage():"Unexpected error";
        session.write(this.builder.create().toJson(new ResponseHeader("onError",false,400,msg,"error")).getBytes(),this.module.label());
    }
}
