package com.tarantula.cci;
import com.google.gson.Gson;
import com.tarantula.*;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.event.IndexEvent;
import com.tarantula.platform.event.ResponsiveEvent;
import com.tarantula.platform.event.ServiceActionEvent;
import com.tarantula.platform.util.SystemUtil;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserEventHandler implements RequestHandler {

    private static TarantulaLogger log = JDKLogger.getLogger(UserEventHandler.class);

    private EventService eventService;
    //private TokenValidator auth;
    private AccessIndexService accessIndexService;
    private String serverTopic;
    private String bucket;
    private final ConcurrentHashMap<String,OnExchange> _hex = new ConcurrentHashMap<>();

    public UserEventHandler(){

    }
    public String name(){
        return "/user";
    }
    public void onRequest(OnExchange onExchange) {
        try{
            String path = onExchange.path();
            String magicKey = onExchange.header(Session.TARANTULA_MAGIC_KEY);
            String tag = onExchange.header(Session.TARANTULA_TAG);
            String action = onExchange.header(Session.TARANTULA_ACTION);
            String  sid = onExchange.id();
            this._hex.put(sid,onExchange);
            //logger.warn(">>>>>>"+action+"/"+path+"/"+magicKey);
            if(path.equals("/user/action")){
                byte[] _payload = onExchange.payload();
                RoutingKey routingKey = eventService.routingKey(magicKey!=null?(this.bucket+"/"+magicKey):(this.bucket+"/"+sid),tag);
                ServiceActionEvent event = new ServiceActionEvent(this.serverTopic,sid,_payload);
                event.clientId(onExchange.header(Session.X_REAL_IP)!=null?onExchange.header(Session.X_REAL_IP):onExchange.remoteAddress());
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
                        byte[] eb = new Gson().toJson(new ResponseHeader("onLogin","wrong login/password combination",false)).getBytes();
                        _hex.remove(sid).onEvent(new ResponsiveEvent("",event.sessionId(),eb,"error",true));
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
                        byte[] eb = new Gson().toJson(new ResponseHeader("onTicket","ticket not exist",false)).getBytes();
                        _hex.remove(sid).onEvent(new ResponsiveEvent("",event.sessionId(),eb,"error",true));
                    }
                }
                else if(action.equals("onRegister")){//to server topic
                    String trackId = this.bucket+Recoverable.PATH_SEPARATOR+ SystemUtil.oid();
                    event.systemId(trackId);
                    RoutingKey _routingKey = eventService.routingKey(trackId,tag);
                    event.destination(_routingKey.route());
                    event.routingNumber(_routingKey.routingNumber());
                    this.eventService.publish(event);
                }
                else if(action.equals("onToken")){//to server topic
                    AccessIndex acc = accessIndexService.get(magicKey);
                    if(acc!=null){
                        event.systemId(acc.distributionKey());
                        RoutingKey _routingKey = eventService.routingKey(acc.distributionKey(),tag);
                        event.destination(_routingKey.route());
                        event.routingNumber(_routingKey.routingNumber());
                    }else{
                        String trackId = this.bucket+Recoverable.PATH_SEPARATOR+ SystemUtil.oid();
                        event.trackId(trackId);
                        RoutingKey _routingKey = eventService.routingKey(trackId,tag);
                        event.destination(_routingKey.route());
                        event.routingNumber(_routingKey.routingNumber());
                    }
                    this.eventService.publish(event);
                }
                else if(action.equals("onReset")){
                    this.eventService.publish(event);
                }
                else{
                    throw new RuntimeException("["+action+"] not supported");
                }
            }
            else if(path.equals("/user/index")){
                RoutingKey routingKey = eventService.routingKey((this.bucket+"/"+sid),tag);
                IndexEvent indexEvent = new IndexEvent(this.serverTopic,sid);
                indexEvent.destination(routingKey.route());
                indexEvent.action("index");
                this.eventService.publish(indexEvent);
            }
            else if(path.equals("/user/view")){
                RoutingKey routingKey = eventService.routingKey((this.bucket+"/"+sid),tag);
                IndexEvent indexEvent = new IndexEvent(this.serverTopic,sid);
                indexEvent.destination(routingKey.route());
                indexEvent.action("view");
                indexEvent.viewId = onExchange.header(Session.TARANTULA_VIEW_ID);
                this.eventService.publish(indexEvent);
            }
        }catch (Exception ex){
            ex.printStackTrace();
            _hex.remove(onExchange.id());
            onExchange.onError(ex,"bad request");
        }
    }

    @Override
    public void start() throws Exception {
        this.serverTopic = UUID.randomUUID().toString();
        this.eventService.registerEventListener(this.serverTopic,this);
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

    @Override
    public void setup(TokenValidator tokenValidator, EventService eventService,AccessIndexService accessIndexService,String bucket) {
        //this.auth = tokenValidator;
        this.eventService = eventService;
        this.accessIndexService = accessIndexService;
        this.bucket = bucket;
    }
    public void onCheck(){
        //log.warn("Total active session ["+_hex.size()+"] on ["+name()+"]");
    }
}
