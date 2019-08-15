package com.tarantula.platform.playmode;

import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.platform.TarantulaApplicationHeader;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yinghu lu on 7/31/2019.
 */
public class SingletonModuleApplication extends TarantulaApplicationHeader implements SchedulingTask {

    private ConcurrentHashMap<String,Session> _onStream = new ConcurrentHashMap<>();

    private long SERVER_PUSH_INTERVAL = 50;

    private Module module;
    private DeploymentServiceProvider serviceProvider;
    private long pendingTimer;

    @Override
    public void callback(Session session, byte[] payload) throws Exception {
        if(session.action().equals("onStream")){
            this._onStream.put(session.systemId(),session);
        }
        else if(session.action().equals("onReset")){
            serviceProvider.reset(this.descriptor);
            session.write(payload,this.module.label());
        }
        else{
            if(this.module.onRequest(session,payload,(delta)->{
                this._onStream.forEach((k,v)->{
                    v.write(delta,this.module.label());
                });
                //server push
            })){
                //clean up on leave
                //this.onTimeout(session);
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
        this.context.log("Dynamic Module Started On ["+descriptor.moduleName()+"]", OnLog.INFO);
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
                this.module.onTimer((delta)->
                    _onStream.forEach((k,v)->
                        v.write(delta,module.label())
                    )
                );
                pendingTimer = descriptor.timerOnModule();
            }
        }catch (Exception ex){
            //ignore it
        }
    }
    public boolean onEvent(Event event){
        try{
            this.module.onJoin(event);
        }catch (Exception ex){
            //write error to client
        }
        return false;
    }
}
