package com.tarantula.platform.module;

import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.TarantulaApplicationHeader;
import com.tarantula.platform.service.DeploymentServiceProvider;
import com.tarantula.platform.util.RingBuffer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * Created by yinghu lu on 7/20/2019.
 */
public class DynamicModuleApplication extends TarantulaApplicationHeader implements Session.TimeoutListener, SchedulingTask,OnInstance.Listener {

    private ConcurrentHashMap<String,Session> _onStream = new ConcurrentHashMap<>();

    private long SERVER_PUSH_INTERVAL = 50;

    private Module module;
    private DeploymentServiceProvider serviceProvider;
    private long pendingTimer;

    private ScheduledFuture timerSchedule;
    private RingBuffer<Connection> cBuffer;
    private Connection current;
    @Override
    public void initialize(Session session) throws Exception {
        session.joined(true);
        module.onJoin(session,(cid,uid,delta)->{
            //pushEvent(uid,delta);
            session.index(parseUid(uid));
            _onStream.put(session.systemId(),session);
            //broadcasting to all streaming session if no udp publisher
            this._onStream.forEach((k,v)->{
                if(v.streaming()&&v.index().equals(parseUid(uid))){
                    v.write(delta,this.module.label()+"#"+uid);
                }
            });
        });
    }
    @Override
    public void callback(Session session, byte[] payload) throws Exception {
        if(session.streaming()){
            Session ex = _onStream.remove(session.systemId());
            if(ex!=null){
                session.index(ex.index());
            }
            this._onStream.put(session.systemId(),session);
        }
        if(this.module.onRequest(session,payload,((cid,uid,delta) -> {
            //pushEvent(uid,delta);
            //broadcasting to all streaming session if no udp publisher
            this._onStream.forEach((k,v)->{
                if(v.streaming()&&parseUid(uid).equals(v.index())){
                    v.write(delta,this.module.label()+"#"+uid);
                }
            });
            //server push
        }))){
            //clean up on leave
            this.onTimeout(session);
        }
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        this.cBuffer = new RingBuffer<>(new Connection[5]);
        this.serviceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        this.context.onRegistry().registerOnInstanceListener(this);
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
            this.module.onTimeout(session,(cid,uid,delta)->{
                //pushEvent(uid,delta);
            });
            this.context.onRegistry().onLeave(session);
        }
    }

    @Override
    public void onIdle(Session session){
        if(!this.descriptor.singleton()){
            this.module.onIdle(session,(cid,uid,delta)->{
                //pushEvent(uid,delta);
                Session pending = _onStream.get(session.systemId());
                if(pending!=null){
                    pending.write(delta,module.label()+"#"+uid);
                }
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
            pendingTimer = pendingTimer-SERVER_PUSH_INTERVAL;
            if(pendingTimer<=0){
                this.module.onTimer(((cid,uid,delta) ->{
                        //pushEvent(uid,delta);
                        _onStream.forEach((k,v)-> {
                            if(v.streaming()&&v.index().equals(parseUid(uid))){
                                v.write(delta, module.label() + "#" + uid);
                            }
                        });
                    }
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
        //this.context.log(session.toString(),ex,OnLog.ERROR);
        String msg = ex.getMessage()!=null?ex.getMessage():"Unexpected error";
        session.write(this.builder.create().toJson(new ResponseHeader("onError",false,400,msg,"error")).getBytes(),this.module.label());
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

    @Override
    public void onUpdated(OnInstance onInstance) {
        if(!onInstance.joined()){
            this._onStream.remove(onInstance.systemId());
        }
    }
    @Override
    public void onState(Connection c) {
        if(c.type().equals(Connection.WEB_SOCKET)){
            this.context.log(c.type()+"/"+c.serverId()+"/"+(c.disabled()?"closed":"open")+"/ on application ["+descriptor.name()+"]",OnLog.WARN);
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
                current = cBuffer.pop();
                module.onConnection(current);
            }
        }
    }
}
