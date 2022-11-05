package com.tarantula.cci;
import com.google.gson.GsonBuilder;
import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.event.ResponsiveEvent;
import com.tarantula.platform.event.ServiceActionEvent;
import com.tarantula.platform.util.ResponseSerializer;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class UserEventHandler extends AbstractRequestHandler implements AccessIndexService.Listener{

    private static TarantulaLogger log = JDKLogger.getLogger(UserEventHandler.class);

    private EventService eventService;
    private AccessIndexService accessIndexService;
    private String serverTopic;
    private String bucket;
    private GsonBuilder builder;
    private final ConcurrentHashMap<String,OnExchange> _hex = new ConcurrentHashMap<>();
    private DeploymentServiceProvider deploymentServiceProvider;
    private TokenValidatorProvider tokenValidatorProvider;

    private AtomicBoolean onIndex;

    public UserEventHandler(){

    }
    public String name(){
        return USER_PATH;
    }
    public void onRequest(OnExchange onExchange) throws Exception {

        String path = onExchange.path();
        String magicKey = onExchange.header(Session.TARANTULA_MAGIC_KEY);
        String name = onExchange.header(Session.TARANTULA_NAME);
        String tag = onExchange.header(Session.TARANTULA_TAG);
        String action = onExchange.header(Session.TARANTULA_ACTION);
        String typeId = onExchange.header(Session.TARANTULA_TYPE_ID);
        String accessKey = onExchange.header(Session.TARANTULA_ACCESS_KEY);
        String  sid = onExchange.id();
        this._hex.put(sid,onExchange);
        if(path.equals("/user/action")){
            byte[] _payload = onExchange.payload();
            RoutingKey routingKey = eventService.routingKey(magicKey!=null?(this.bucket+"/"+magicKey):(this.bucket+"/"+sid),tag);
            ServiceActionEvent event = new ServiceActionEvent(this.serverTopic,sid,_payload);
            event.action(action);
            event.routingNumber(routingKey.routingNumber());
            event.destination(routingKey.route());
            if(action.equals("onLogin")){
                AccessIndex acc = accessIndexService.get(magicKey);
                if(acc!=null){
                    event.systemId(acc.distributionKey());
                    RoutingKey _routingKey = eventService.routingKey(acc.distributionKey(),tag);
                    event.destination(_routingKey.route());
                    event.routingNumber(_routingKey.routingNumber());
                    this.eventService.publish(event);
                }else{
                    //send login failed back
                    byte[] eb = this.builder.create().toJson(new ResponseHeader("onLogin","wrong login/password combination",false)).getBytes();
                    _hex.remove(sid).onEvent(new ResponsiveEvent("",event.sessionId(),eb,true));
                }
            }
            else if(action.equals("onToken")){//to server topic
                AccessIndex acc = accessIndexService.get(magicKey);
                if(acc!=null){//existing entry
                    event.systemId(acc.distributionKey());
                    RoutingKey _routingKey = eventService.routingKey(acc.distributionKey(),tag);
                    event.destination(_routingKey.route());
                    event.routingNumber(_routingKey.routingNumber());
                    this.eventService.publish(event);
                }
                else if(onIndex.get()){//third party token exchange first time
                    event.action("onTokenRegister");
                    AccessIndex _tindex = accessIndexService.set(magicKey,0);
                    if(_tindex!=null){
                        event.systemId(_tindex.distributionKey());
                        RoutingKey _routingKey = eventService.routingKey(_tindex.distributionKey(),tag);
                        event.destination(_routingKey.route());
                        event.routingNumber(_routingKey.routingNumber());
                        this.eventService.publish(event);
                    }
                    else{
                        byte[] eb = this.builder.create().toJson(new ResponseHeader("onTokenRegister","["+magicKey+"] not available",false)).getBytes();
                        _hex.remove(sid).onEvent(new ResponsiveEvent("",event.sessionId(),eb,true));
                    }
                }
                else{
                    byte[] eb = this.builder.create().toJson(new ResponseHeader("onToken","service not available,will be back shortly",false)).getBytes();
                    _hex.remove(sid).onEvent(new ResponsiveEvent("",event.sessionId(),eb,true));
                }
            }
            else if(action.equals("onTicket")){
                AccessIndex acc = accessIndexService.get(magicKey);
                if(acc!=null){
                    event.systemId(acc.distributionKey());
                    RoutingKey _routingKey = eventService.routingKey(acc.distributionKey(),tag);
                    event.destination(_routingKey.route());
                    event.routingNumber(_routingKey.routingNumber());
                    this.eventService.publish(event);
                }else{
                    byte[] eb = this.builder.create().toJson(new ResponseHeader("onTicket","ticket not exist",false)).getBytes();
                    _hex.remove(sid).onEvent(new ResponsiveEvent("",event.sessionId(),eb,true));
                }
            }
            else if(action.equals("onRegister")){//to server topic
                if(onIndex.get()){ //register
                    AccessIndex _aindex = this.accessIndexService.set(magicKey,0);
                    if(_aindex!=null){
                        event.systemId(_aindex.distributionKey());
                        RoutingKey _routingKey = eventService.routingKey(_aindex.distributionKey(),tag);
                        event.destination(_routingKey.route());
                        event.routingNumber(_routingKey.routingNumber());
                        this.eventService.publish(event);
                    }else{
                        byte[] eb = this.builder.create().toJson(new ResponseHeader("onRegister","["+magicKey+"] not available",false)).getBytes();
                        _hex.remove(sid).onEvent(new ResponsiveEvent("",event.sessionId(),eb,true));
                    }
                }
                else{
                    byte[] eb = this.builder.create().toJson(new ResponseHeader("onToken","service not available,will be back shortly",false)).getBytes();
                    _hex.remove(sid).onEvent(new ResponsiveEvent("",event.sessionId(),eb,true));
                }
            }
            else if(action.equals("onDevice")){//to server topic
                AccessIndex acc = accessIndexService.get(magicKey);
                if(acc!=null){
                    event.systemId(acc.distributionKey());
                    RoutingKey _routingKey = eventService.routingKey(acc.distributionKey(),tag);
                    event.destination(_routingKey.route());
                    event.routingNumber(_routingKey.routingNumber());
                    this.eventService.publish(event);
                }else if(onIndex.get()){//device login exchange first time
                    AccessIndex _dindex = accessIndexService.set(magicKey,0);
                    if(_dindex!=null){
                        event.action("onDeviceRegister");
                        event.systemId(_dindex.distributionKey());
                        RoutingKey _routingKey = eventService.routingKey(_dindex.distributionKey(),tag);
                        event.destination(_routingKey.route());
                        event.routingNumber(_routingKey.routingNumber());
                        this.eventService.publish(event);
                    }
                    else{
                        byte[] eb = this.builder.create().toJson(new ResponseHeader("onDeviceRegister","["+magicKey+"] not available",false)).getBytes();
                        _hex.remove(sid).onEvent(new ResponsiveEvent("",event.sessionId(),eb,true));
                    }
                }
                else{
                    byte[] eb = this.builder.create().toJson(new ResponseHeader("onToken","service not available,will be back shortly",false)).getBytes();
                    _hex.remove(sid).onEvent(new ResponsiveEvent("",event.sessionId(),eb,true));
                }
            }
            else if(action.equals("onResetCode")){
                event.trackId(name);
                this.eventService.publish(event);
            }
            else if(action.equals("onResetPassword")){
                AccessIndex acc = accessIndexService.get(magicKey);
                if(acc!=null){
                    event.systemId(acc.distributionKey());
                    RoutingKey _routingKey = eventService.routingKey(acc.distributionKey(),tag);
                    event.destination(_routingKey.route());
                    event.routingNumber(_routingKey.routingNumber());
                    this.eventService.publish(event);
                }else{
                    //send login failed back
                    byte[] eb = this.builder.create().toJson(new ResponseHeader("onResetPassword","wrong login/password combination",false)).getBytes();
                    _hex.remove(sid).onEvent(new ResponsiveEvent("",event.sessionId(),eb,true));
                }
            }
            else if(action.equals("onAvailable")){
                event.trackId(typeId);
                this.eventService.publish(event);
            }
            else if(action.equals("onIndex")){
                this.eventService.publish(event);
            }
            else if(action.equals("onDeveloper")){
                GameCluster validTypeId = this.tokenValidatorProvider.validateGameClusterAccessKey(accessKey);
                if(validTypeId==null) throw new RuntimeException("Illegal access");
                AccessIndex acc = accessIndexService.get(magicKey);
                if(acc!=null){
                    event.systemId(acc.distributionKey());
                    RoutingKey _routingKey = eventService.routingKey(acc.distributionKey(),tag);
                    event.trackId(accessKey);
                    event.destination(_routingKey.route());
                    event.routingNumber(_routingKey.routingNumber());
                    this.eventService.publish(event);
                }else if(onIndex.get()){//device login exchange first time
                    AccessIndex _dindex = accessIndexService.set(magicKey,0);
                    if(_dindex!=null){
                        event.action("onDeveloperRegister");
                        event.trackId(accessKey);
                        event.systemId(_dindex.distributionKey());
                        RoutingKey _routingKey = eventService.routingKey(_dindex.distributionKey(),tag);
                        event.destination(_routingKey.route());
                        event.routingNumber(_routingKey.routingNumber());
                        this.eventService.publish(event);
                    }
                    else{
                        byte[] eb = this.builder.create().toJson(new ResponseHeader("onDeveloperRegister","["+magicKey+"] not available",false)).getBytes();
                        _hex.remove(sid).onEvent(new ResponsiveEvent("",event.sessionId(),eb,true));
                    }
                }
                else{
                    byte[] eb = this.builder.create().toJson(new ResponseHeader("onToken","service not available,will be back shortly",false)).getBytes();
                    _hex.remove(sid).onEvent(new ResponsiveEvent("",event.sessionId(),eb,true));
                }
            }
            else{
                throw new RuntimeException("["+action+"] not supported");
            }
        }
    }

    @Override
    public void start() throws Exception {
        this.serverTopic = UUID.randomUUID().toString();
        this.eventService.registerEventListener(this.serverTopic,this);
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(ResponseHeader.class,new ResponseSerializer());
        log.info("User handler started");
    }

    @Override
    public void shutdown() throws Exception {

    }
    @Override
    public boolean onEvent(Event event) {
        OnExchange hx = this._hex.remove(event.sessionId());
        if(hx!=null){
            //logger.warn("user event ["+event.sessionId()+"]");
            hx.onEvent(event);
        }
        return true;
    }
    public void setup(ServiceContext tcx){
        this.onIndex = new AtomicBoolean(false);
        this.eventService = tcx.eventService();
        this.deploymentServiceProvider = tcx.deploymentServiceProvider();
        this.deploymentServiceProvider.registerAccessIndexListener(this);
        this.accessIndexService = tcx.accessIndexService();
        this.tokenValidatorProvider = (TokenValidatorProvider) tcx.serviceProvider(TokenValidatorProvider.NAME);
        this.bucket = tcx.node().bucket();
    }
    public void onCheck(){
        //log.warn("Total active session ["+_hex.size()+"] on ["+name()+"]");
    }

    @Override
    public void onStop() {
        log.warn("access index stopped");
        onIndex.set(false);
    }

    @Override
    public void onStart() {
        log.warn("access index started");
        onIndex.set(true);
    }
}
