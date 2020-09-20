package com.tarantula.platform.module;

import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.platform.TarantulaApplicationHeader;
import com.tarantula.platform.event.FastPlayEvent;
import com.tarantula.platform.service.DeploymentServiceProvider;
import com.tarantula.platform.util.RingBuffer;
import java.util.concurrent.ScheduledFuture;

/**
 * Updated by yinghu lu on 4/19/2020.
 */
public class SingletonModuleApplication extends TarantulaApplicationHeader implements SchedulingTask {

    private long SERVER_PUSH_INTERVAL;

    private Module module;
    private DeploymentServiceProvider serviceProvider;
    private RingBuffer<Connection> cBuffer;
    private Connection current;
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
        this.cBuffer = new RingBuffer<>(new Connection[5]);
        this.serviceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        this.module = this.serviceProvider.module(this.descriptor);
        SERVER_PUSH_INTERVAL = descriptor.timerOnModule();
        if(SERVER_PUSH_INTERVAL>0){
            this.serviceProvider.registerOnConnectionListener(this);
            this.scheduledFuture = this.context.schedule(this);
        }
        module.setup(context);
        //this.context.log("Singleton Dynamic Module Started On ["+descriptor.moduleName()+"]", OnLog.INFO);
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
            this.module.onTimer(((cid,uid,delta) ->
                    this.serviceProvider.registerPostOffice().onConnection(cid).send(this.module.label()+"#"+uid,delta)
            ));
        }catch (Exception ex){
            //ignore it
            this.context.log("error",ex,OnLog.ERROR);
        }
    }
    public boolean onEvent(Event event){
        try{
            if(event instanceof FastPlayEvent){
                this.module.onJoin(event,(cid,uid,delta)->
                    this.serviceProvider.registerPostOffice().onConnection(cid).send(this.module.label()+"#"+uid,delta)
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
        if(c.type().equals(Connection.WEB_SOCKET)){
            this.context.log(c.type()+"/"+c.serverId()+"/"+(c.disabled()?"closed":"open")+"/ on lobby ["+descriptor.tag()+"]",OnLog.WARN);
            onWebSocket(c);
        }
    }
    private void onWebSocket(Connection c) {
        if(!c.disabled()){
            if(!cBuffer.push(c)){
                cBuffer.reset(((ca,limit)->{
                    Connection[] cn = new Connection[ca.length*2];
                    for(int i=0;i<limit;i++){
                        cn[i]=ca[i];
                    }
                    cn[limit]=c;
                    return cn;
                }));
            }
            if(current==null){
                current = cBuffer.pop();
                module.onConnection(current);
            }
        }
        else{
            cBuffer.reset((ca,limit)->{
                Connection[] cn = new Connection[ca.length];
                int r=0;
                for(int i=0;i<limit;i++){
                    if(!(ca[i].serverId().equals(c.serverId()))){
                        cn[r++]=ca[i];
                    }
                }
                return cn;
            });
            if(current!=null&&current.serverId().equals(c.serverId())){
                current.disabled(true);
                module.onConnection(current);
                current = cBuffer.pop();
                if(current!=null){
                    module.onConnection(current);
                }
            }
        }
    }
}
