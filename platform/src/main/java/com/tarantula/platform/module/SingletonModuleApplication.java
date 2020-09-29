package com.tarantula.platform.module;

import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.platform.TarantulaApplicationHeader;
import com.tarantula.platform.event.FastPlayEvent;
import com.tarantula.platform.service.DeploymentServiceProvider;
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
        if(this.module.onRequest(session,payload,((cid,uid,delta) ->{
            this.serviceProvider.registerPostOffice().onConnection(cid).send(this.module.label()+"#"+uid,delta);
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
            this.serviceProvider.registerOnConnectionListener(this);
            this.scheduledFuture = this.context.schedule(this);
        }
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
            this.context.log("error",ex,OnLog.ERROR);
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
    public void onState(Connection c) {
        this.context.log(c.type()+"/"+c.serverId()+"/"+(c.disabled()?"closed":"open")+"/ on lobby ["+descriptor.tag()+"]",OnLog.WARN);
        this.module.onConnection(c);
    }
}
