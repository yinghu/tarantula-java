package com.tarantula.platform.module;

import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.platform.TarantulaApplicationHeader;
import com.tarantula.platform.service.DeploymentServiceProvider;
import com.tarantula.platform.util.RingBuffer;

import java.util.concurrent.ScheduledFuture;

/**
 * Update by yinghu lu on 5/7/2020.
 */
public class DynamicModuleApplication extends TarantulaApplicationHeader implements Session.TimeoutListener, SchedulingTask,OnInstance.Listener {

    private long SERVER_PUSH_INTERVAL = 50;

    private Module module;
    private DeploymentServiceProvider serviceProvider;
    //private long pendingTimer;

    private ScheduledFuture timerSchedule;
    //private RingBuffer<Connection> cBuffer;
   //private Connection current;
    @Override
    public void initialize(Session session) throws Exception {
        session.joined(true);
        module.onJoin(session,(cid,uid,delta)->{
            this.serviceProvider.registerPostOffice().onConnection(cid).send(module.label()+"#"+uid,delta);
        });
    }
    @Override
    public void callback(Session session, byte[] payload) throws Exception {
        if(this.module.onRequest(session,payload,((cid,uid,delta) -> {
            this.serviceProvider.registerPostOffice().onConnection(cid).send(module.label()+"#"+uid,delta);
            //server push
        }))){
            //clean up on leave
            this.onTimeout(session);
        }
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        //this.cBuffer = new RingBuffer<>(new Connection[5]);
        this.serviceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        this.context.onRegistry().registerOnInstanceListener(this);
        try{
            module = this.serviceProvider.module(this.descriptor);
            this.SERVER_PUSH_INTERVAL = descriptor.timerOnModule();
            if(this.SERVER_PUSH_INTERVAL>0){
                this.serviceProvider.registerOnConnectionListener(this);
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
            this.module.onTimeout(session,(cid,uid,delta)->{
                this.serviceProvider.registerPostOffice().onConnection(cid).send(module.label()+"#"+uid,delta);
            });
            this.context.onRegistry().onLeave(session);
        }
    }

    @Override
    public void onIdle(Session session){
        if(!this.descriptor.singleton()){
            this.module.onIdle(session,(cid,uid,delta)->{
                this.serviceProvider.registerPostOffice().onConnection(cid).send(module.label()+"#"+uid,delta);
            });
        }
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
                this.serviceProvider.registerPostOffice().onConnection(cid).send(module.label()+"#"+uid,delta)
            ));

        }catch (Exception ex){
            //ignore it
            this.context.log("error",ex,OnLog.ERROR);
        }
    }
    @Override
    public void clear(){
       this.module.clear();
        if(timerSchedule!=null){
            timerSchedule.cancel(true);
        }
        this.context.log("Instance ["+descriptor.moduleName()+"/"+this.context.onRegistry().distributionKey()+"] closed",OnLog.WARN);
    }

    @Override
    public void onUpdated(OnInstance onInstance) {
        if(!onInstance.joined()){

        }
    }
    @Override
    public void onState(Connection c) {
        //if(c.type().equals(Connection.WEB_SOCKET)){
            this.context.log(c.type()+"/"+c.serverId()+"/"+(c.disabled()?"closed":"open")+"/ on application ["+descriptor.name()+"]",OnLog.WARN);
            //onWebSocket(c);
            module.onConnection(c);
        //}
    }
    /**
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
    }**/
}
