package com.tarantula.platform.module;

import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.TarantulaApplicationHeader;
import com.tarantula.platform.event.FastPlayEvent;
import com.tarantula.platform.service.DeploymentServiceProvider;
import com.tarantula.platform.util.RingBuffer;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Updated by yinghu lu on 4/19/2020.
 */
public class SingletonModuleApplication extends TarantulaApplicationHeader implements SchedulingTask {

    private ConcurrentHashMap<String,ConcurrentHashMap<String,Session>> _onIndex = new ConcurrentHashMap<>();

    private long SERVER_PUSH_INTERVAL;

    private Module module;
    private DeploymentServiceProvider serviceProvider;
    private RingBuffer<Connection> cBuffer;
    private Connection current;
    @Override
    public void callback(Session session, byte[] payload) throws Exception {
        if(session.streaming()){
            this._onIndex.computeIfAbsent(session.instanceId(),(k)->new ConcurrentHashMap<>()).put(session.systemId(),session);
        }
        else if(this.module.onRequest(session,payload,((uid,delta) ->{
            _onIndex.computeIfAbsent(parseUid(uid),(k)->new ConcurrentHashMap<>()).forEach((k,v)->{
                v.write(delta,this.module.label()+"#"+uid);
            });
        }))){
            //clean up on leave
            if(session.instanceId()!=null&&_onIndex.containsKey(session.instanceId())){
                Session rm = this._onIndex.get(session.instanceId()).remove(session.systemId());
                if(rm!=null){
                    ResponseHeader resp = new ResponseHeader(session.action(),"close session");
                    rm.write(this.builder.create().toJson(resp).getBytes(),module.label()+"#"+session.instanceId()+"?"+session.action(),true);
                }
            }
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
            this.module.onTimer(((uid,delta) -> {
                    if(delta==null){
                        _onIndex.remove(parseUid(uid)).forEach((k,v)->{
                            ResponseHeader resp = new ResponseHeader("onEnd","close session");
                            v.write(this.builder.create().toJson(resp).getBytes(),module.label()+"#"+uid,true);
                        });
                        return;
                    }
                    _onIndex.computeIfAbsent(parseUid(uid),(k)->new ConcurrentHashMap<>()).forEach((k,v)->{
                        v.write(delta,this.module.label()+"#"+uid);
                    });
                    //this.serviceProvider.registerPostOffice().onConnection("serverId").send();
                }
            ));
        }catch (Exception ex){
            //ignore it
            this.context.log("error",ex,OnLog.ERROR);
        }
    }
    public boolean onEvent(Event event){
        try{
            if(event instanceof FastPlayEvent){
                this.module.onJoin(event,(uid,delta)->{
                    _onIndex.computeIfAbsent(parseUid(uid),(k)->new ConcurrentHashMap<>()).forEach((k,v)->{
                        v.write(delta,this.module.label()+"#"+uid);
                    });
                });
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
