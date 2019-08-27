package com.tarantula.cci;

import com.tarantula.*;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.OnSessionTrack;
import com.tarantula.platform.event.ServiceActionEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class ServiceEventHandler implements RequestHandler {

	private static final JDKLogger log = JDKLogger.getLogger(ServiceEventHandler.class);

    private EventService eventService;
    private TokenValidator auth;
    //private AccessIndexService accessIndexService;
    private String serverTopic;
    private String bucket;
    private final ConcurrentHashMap<String,OnExchange> _hex = new ConcurrentHashMap<>();

    public ServiceEventHandler(){

	}
    public String name(){
        return "/service";
    }
    public void onRequest(OnExchange exchange){
            try{
                String path = exchange.path();
                String token = exchange.header(Session.TARANTULA_TOKEN);//authenticated token
                String action = exchange.header(Session.TARANTULA_ACTION);
                String tag = exchange.header(Session.TARANTULA_TAG);
                byte[]  _payload = exchange.payload();
                //String clientId = exchange.header(Session.X_REAL_IP)!=null?exchange.header(Session.X_REAL_IP):exchange.remoteAddress();
                String sid = exchange.id();
                this._hex.put(sid,exchange);
                if(path.startsWith("/service/action")){
                    OnSession id = new OnSessionTrack();//place holder for public access applications
                    RoutingKey routingKey = eventService.routingKey(this.bucket+"/"+sid,tag);
                    if((token!=null)&&(!token.equals("undefined"))){
                        id = auth.validToken(token);//first entry point check
                        routingKey = eventService.routingKey(id.systemId(),tag);
                    }
                    ServiceActionEvent actionEvent = new ServiceActionEvent(this.serverTopic,sid,_payload);
                    actionEvent.systemId(id.systemId());
                    actionEvent.stub(id.stub());
                    actionEvent.ticket(id.ticket());
                    actionEvent.trackId(id.oid());
                    actionEvent.action(action!=null?action:"");
                    actionEvent.routingNumber(routingKey.routingNumber());
                    actionEvent.destination(routingKey.route());
                    actionEvent.streaming(exchange.streaming());
                    //log.warn("Routing Number->"+actionEvent.routingNumber()+"/"+actionEvent.forwarding()+"/"+actionEvent.destination()+"/");
                    this.eventService.publish(actionEvent);
                }
                else{
                    throw new UnsupportedOperationException("HTTP ["+exchange.method()+"] request ["+path+"] not supported");
                }

            }catch(Exception ex){
                ex.printStackTrace();
                _hex.remove(exchange.id());
                exchange.onError(ex,"bad request");
            }

    }

    @Override
    public void start() throws Exception {
        this.serverTopic = UUID.randomUUID().toString();
        this.eventService.registerEventListener(this.serverTopic,this);
        log.info("Application handler started");
    }

    @Override
    public void shutdown() throws Exception {

    }
    @Override
    public boolean onEvent(Event event) {
        OnExchange hx = this._hex.get(event.sessionId());
        if(hx!=null){
            if(hx.onEvent(event)){
                _hex.remove(event.sessionId());
            }
        }
        return true;
    }

    @Override
    public void setup(TokenValidator tokenValidator, EventService eventService, AccessIndexService accessIndexService,String bucket,DeploymentServiceProvider deploymentServiceProvider) {
        this.auth = tokenValidator;
        this.eventService = eventService;
        //this.accessIndexService = accessIndexService;
        this.bucket = bucket;
    }
    public void onCheck(){
        //log.warn("Total active session ["+_hex.size()+"] on ["+name()+"]");
    }
}
