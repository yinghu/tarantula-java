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


import java.util.concurrent.atomic.AtomicBoolean;

public class UserEventHandler extends AbstractRequestHandler implements AccessIndexService.Listener{

    private static TarantulaLogger log = JDKLogger.getLogger(UserEventHandler.class);
    private final static String METRICS_CATEGORY = "httpUserCount";
    private AccessIndexService accessIndexService;
    private String bucket;
    private GsonBuilder builder;

    private DeploymentServiceProvider deploymentServiceProvider;

    private AtomicBoolean onIndex;

    public UserEventHandler(){
        super(true);
    }
    public String name(){
        return USER_PATH;
    }
    public void onRequest(OnExchange onExchange) throws Exception {
        super.onRequest(onExchange);
        String path = onExchange.path();
        String magicKey = onExchange.header(Session.TARANTULA_MAGIC_KEY);
        String name = onExchange.header(Session.TARANTULA_NAME);
        String tag = onExchange.header(Session.TARANTULA_TAG);
        String action = onExchange.header(Session.TARANTULA_ACTION);
        String typeId = onExchange.header(Session.TARANTULA_TYPE_ID);
        String accessKey = onExchange.header(Session.TARANTULA_ACCESS_KEY);
        if(path.equals("/user/action")){
            byte[] _payload = onExchange.payload();
            RoutingKey routingKey = eventService.routingKey(magicKey!=null?(this.bucket+"/"+magicKey):(this.bucket+"/"+onExchange.id()),tag);
            ServiceActionEvent event = new ServiceActionEvent(this.serviceTopic,onExchange.id(),_payload);
            event.action(action);
            event.routingNumber(routingKey.routingNumber());
            event.destination(routingKey.route());
            if(action.equals("onLogin")){
                AccessIndex acc = accessIndexService.get(magicKey);
                if(acc!=null){
                    event.distributionId(acc.distributionId());
                    //event.systemId(acc.distributionKey());
                    RoutingKey _routingKey = eventService.routingKey(acc.distributionId(),tag);
                    event.destination(_routingKey.route());
                    event.routingNumber(_routingKey.routingNumber());
                    this.eventService.publish(event);
                }else{
                    //send login failed back
                    byte[] eb = this.builder.create().toJson(new ResponseHeader("onLogin","wrong login/password combination",false)).getBytes();
                    super.onEvent(new ResponsiveEvent("",event.sessionId(),eb,true));
                }
            }
            else if(action.equals("onToken")){//to server topic
                AccessIndex acc = accessIndexService.get(magicKey);
                if(acc!=null){//existing entry
                    event.distributionId(acc.distributionId());
                    RoutingKey _routingKey = eventService.routingKey(acc.distributionId(),tag);
                    event.destination(_routingKey.route());
                    event.routingNumber(_routingKey.routingNumber());
                    this.eventService.publish(event);
                }
                else if(onIndex.get()){//third party token exchange first time
                    event.action("onTokenRegister");
                    AccessIndex _tindex = accessIndexService.set(magicKey,AccessIndex.THIRD_PARTY_LOGIN_INDEX);
                    if(_tindex!=null){
                        event.distributionId(_tindex.distributionId());
                        RoutingKey _routingKey = eventService.routingKey(_tindex.distributionId(),tag);
                        event.destination(_routingKey.route());
                        event.routingNumber(_routingKey.routingNumber());
                        this.eventService.publish(event);
                    }
                    else{
                        byte[] eb = this.builder.create().toJson(new ResponseHeader("onTokenRegister","["+magicKey+"] not available",false)).getBytes();
                        super.onEvent(new ResponsiveEvent("",event.sessionId(),eb,true));
                    }
                }
                else{
                    byte[] eb = this.builder.create().toJson(new ResponseHeader("onToken","service not available,will be back shortly",false)).getBytes();
                    super.onEvent(new ResponsiveEvent("",event.sessionId(),eb,true));
                }
            }
            else if(action.equals("onTicket")){
                AccessIndex acc = accessIndexService.get(magicKey);
                if(acc!=null){
                    event.distributionId(acc.distributionId());
                    RoutingKey _routingKey = eventService.routingKey(acc.distributionId(),tag);
                    event.destination(_routingKey.route());
                    event.routingNumber(_routingKey.routingNumber());
                    this.eventService.publish(event);
                }else{
                    byte[] eb = this.builder.create().toJson(new ResponseHeader("onTicket","ticket not exist",false)).getBytes();
                    super.onEvent(new ResponsiveEvent("",event.sessionId(),eb,true));
                }
            }
            else if(action.equals("onRegister")){//to server topic
                if(onIndex.get()){ //register
                    AccessIndex _aindex = this.accessIndexService.set(magicKey,AccessIndex.USER_INDEX);
                    if(_aindex!=null){
                        event.distributionId(_aindex.distributionId());
                        RoutingKey _routingKey = eventService.routingKey(_aindex.distributionId(),tag);
                        event.destination(_routingKey.route());
                        event.routingNumber(_routingKey.routingNumber());
                        this.eventService.publish(event);
                    }else{
                        byte[] eb = this.builder.create().toJson(new ResponseHeader("onRegister","["+magicKey+"] not available",false)).getBytes();
                        super.onEvent(new ResponsiveEvent("",event.sessionId(),eb,true));
                    }
                }
                else{
                    byte[] eb = this.builder.create().toJson(new ResponseHeader("onToken","service not available,will be back shortly",false)).getBytes();
                    super.onEvent(new ResponsiveEvent("",event.sessionId(),eb,true));
                }
            }
            else if(action.equals("onDevice")){//to server topic
                AccessIndex acc = accessIndexService.get(magicKey);
                if(acc!=null){
                    event.distributionId(acc.distributionId());
                    RoutingKey _routingKey = eventService.routingKey(acc.distributionId(),tag);
                    event.destination(_routingKey.route());
                    event.routingNumber(_routingKey.routingNumber());
                    this.eventService.publish(event);
                }else if(onIndex.get()){//device login exchange first time
                    AccessIndex _dindex = accessIndexService.set(magicKey,AccessIndex.DEVICE_LOGIN_INDEX);
                    if(_dindex!=null){
                        event.action("onDeviceRegister");
                        event.distributionId(_dindex.distributionId());
                        RoutingKey _routingKey = eventService.routingKey(_dindex.distributionId(),tag);
                        event.destination(_routingKey.route());
                        event.routingNumber(_routingKey.routingNumber());
                        this.eventService.publish(event);
                    }
                    else{
                        byte[] eb = this.builder.create().toJson(new ResponseHeader("onDeviceRegister","["+magicKey+"] not available",false)).getBytes();
                        super.onEvent(new ResponsiveEvent("",event.sessionId(),eb,true));
                    }
                }
                else{
                    byte[] eb = this.builder.create().toJson(new ResponseHeader("onDevice","service not available,will be back shortly",false)).getBytes();
                    super.onEvent(new ResponsiveEvent("",event.sessionId(),eb,true));
                }
            }
            else if(action.equals("onResetCode")){
                event.trackId(name);
                this.eventService.publish(event);
            }
            else if(action.equals("onResetPassword")){
                AccessIndex acc = accessIndexService.get(magicKey);
                if(acc!=null){
                    event.distributionId(acc.distributionId());
                    RoutingKey _routingKey = eventService.routingKey(acc.distributionId(),tag);
                    event.destination(_routingKey.route());
                    event.routingNumber(_routingKey.routingNumber());
                    this.eventService.publish(event);
                }else{
                    //send login failed back
                    byte[] eb = this.builder.create().toJson(new ResponseHeader("onResetPassword","wrong login/password combination",false)).getBytes();
                    super.onEvent(new ResponsiveEvent("",event.sessionId(),eb,true));
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
                GameCluster validTypeId = this.tokenValidator.validateGameClusterAccessKey(accessKey);
                if(validTypeId==null) throw new RuntimeException("Illegal access");
                AccessIndex acc = accessIndexService.get(magicKey);
                if(acc!=null){
                    event.distributionId(acc.distributionId());
                    RoutingKey _routingKey = eventService.routingKey(acc.distributionId(),tag);
                    event.trackId(accessKey);
                    event.destination(_routingKey.route());
                    event.routingNumber(_routingKey.routingNumber());
                    this.eventService.publish(event);
                }else if(onIndex.get()){//device login exchange first time
                    AccessIndex _dindex = accessIndexService.set(magicKey,AccessIndex.DEVELOPER_LOGIN_INDEX);
                    if(_dindex!=null){
                        event.action("onDeveloperRegister");
                        event.trackId(accessKey);
                        event.distributionId(_dindex.distributionId());
                        RoutingKey _routingKey = eventService.routingKey(_dindex.distributionId(),tag);
                        event.destination(_routingKey.route());
                        event.routingNumber(_routingKey.routingNumber());
                        this.eventService.publish(event);
                    }
                    else{
                        byte[] eb = this.builder.create().toJson(new ResponseHeader("onDeveloperRegister","["+magicKey+"] not available",false)).getBytes();
                        super.onEvent(new ResponsiveEvent("",event.sessionId(),eb,true));
                    }
                }
                else{
                    byte[] eb = this.builder.create().toJson(new ResponseHeader("onToken","service not available,will be back shortly",false)).getBytes();
                    super.onEvent(new ResponsiveEvent("",event.sessionId(),eb,true));
                }
            }
            else{
                throw new RuntimeException("["+action+"] not supported");
            }
        }
    }

    @Override
    public void start() throws Exception {
        super.start();
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(ResponseHeader.class,new ResponseSerializer());
        log.info("User handler started");
    }



    public void setup(ServiceContext tcx){
        super.setup(tcx);
        this.onIndex = new AtomicBoolean(false);
        this.deploymentServiceProvider = tcx.deploymentServiceProvider();
        this.deploymentServiceProvider.registerAccessIndexListener(this);
        this.accessIndexService = tcx.clusterProvider().accessIndexService();
        this.bucket = tcx.node().bucketName();
    }


    @Override
    public void onStop() {
        log.warn("access index stopped");
        onIndex.set(false);
    }

    @Override
    public void onStart() {
        log.info("access index started");
        onIndex.set(true);
    }

    public String metricsCategory(){
        return METRICS_CATEGORY;
    }
}
