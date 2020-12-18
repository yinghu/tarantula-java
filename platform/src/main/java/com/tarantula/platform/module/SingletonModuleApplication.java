package com.tarantula.platform.module;

import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.tarantula.platform.TarantulaApplicationHeader;
import com.tarantula.platform.event.FastPlayEvent;

import java.util.concurrent.ScheduledFuture;

/**
 * Updated by yinghu lu on 4/19/2020.
 */
public class SingletonModuleApplication extends TarantulaApplicationHeader implements SchedulingTask {

    private long SERVER_PUSH_INTERVAL;

    private Module module;
    private DeploymentServiceProvider serviceProvider;
    private ScheduledFuture scheduledFuture;
    @Override
    public void callback(Session session, byte[] payload) throws Exception {
        if(this.module.onRequest(session,payload,((connection,label,delta) ->{
            this.serviceProvider.registerPostOffice().onConnection(connection).send(label,delta);
        }))){
            //clean up on leave if any
        }
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        this.serviceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        this.module = this.serviceProvider.module(this.descriptor);
        SERVER_PUSH_INTERVAL = descriptor.timerOnModule();
        if(SERVER_PUSH_INTERVAL>0){
            this.scheduledFuture = this.context.schedule(this);
        }
        this.serviceProvider.registerOnConnectionStateListener(this);
        module.setup(context);
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
            this.module.onTimer(((connection,label,delta) ->
                    this.serviceProvider.registerPostOffice().onConnection(connection).send(label,delta)
            ));
        }catch (Exception ex){
            //ignore it
            this.context.log("error",ex, OnLog.ERROR);
        }
    }
    public boolean onEvent(Event event){
        try{
            if(event instanceof FastPlayEvent){
                this.module.onJoin(event,(connection,label,delta)->
                    this.serviceProvider.registerPostOffice().onConnection(connection).send(label,delta)
                );
            }
            else{
                context.log("event->"+event.toString(),OnLog.WARN);
            }
        }catch (Exception ex){
            //write error to client
            this.onError(event,ex);
        }
        return false;
    }
    @Override
    public void onBucket(int bucket,int state){
        //this.context.log("Bucket->"+bucket+"/"+state,OnLog.WARN);
    }
    @Override
    public void clear(){
        this.module.clear();
        if(scheduledFuture!=null){
            scheduledFuture.cancel(true);
        }
    }
    @Override
    public String typeId(){
        return module.label();
    }
    @Override
    public void onState(Connection c) {
        this.context.log(c.type()+"/"+c.serverId()+"/"+(c.disabled()?"closed":"open")+"/ on lobby ["+descriptor.tag()+"//"+descriptor.typeId()+"]",OnLog.WARN);
        this.module.onConnection(c);
    }
}
