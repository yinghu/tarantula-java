package com.tarantula.cci;

import com.tarantula.*;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.event.ApplicationActionEvent;
import com.tarantula.platform.event.ApplicationServiceEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class ApplicationEventHandler  implements RequestHandler {

	private static final JDKLogger log = JDKLogger.getLogger(ApplicationEventHandler.class);

    private EventService eventService;
    private TokenValidator auth;
    private AccessIndexService accessIndexService;
    private String serverTopic;
    private String bucket;
    private final ConcurrentHashMap<String,OnExchange> _hex = new ConcurrentHashMap<>();


    public ApplicationEventHandler(){

	}
    public String name(){
        return "/application";
    }
    public void onRequest(OnExchange exchange){
        try{
            String path = exchange.path();
            String token = exchange.header(Session.TARANTULA_TOKEN);//authenticated token
            String applicationId = exchange.header(Session.TARANTULA_APPLICATION_ID);//application Id
            String instanceId = exchange.header(Session.TARANTULA_INSTANCE_ID);//instance Id
            String tag = exchange.header(Session.TARANTULA_TAG);
            String action = exchange.header(Session.TARANTULA_ACTION);
            byte[]  _payload = exchange.payload();
            //String clientId = exchange.header(Session.X_REAL_IP)!=null?exchange.header(Session.X_REAL_IP):exchange.remoteAddress();
            String sid = exchange.id();
            this._hex.put(sid,exchange);
            OnSession id = auth.validToken(token);
            if(path.startsWith("/application/instance")){
                ApplicationActionEvent magic = new ApplicationActionEvent(this.serverTopic,sid,id.systemId(),applicationId,instanceId,_payload);
                magic.stub(id.stub());
                magic.ticket(id.ticket());
                magic.trackId(id.oid());
                magic.action(action!=null?action:"");
                magic.routingNumber(id.routingNumber());
                magic.destination(eventService.instanceRoutingKey(applicationId,instanceId).route());
                magic.streaming(exchange.streaming());
                this.eventService.publish(magic);
            }
            else if(path.startsWith("/application/service")){
                ApplicationServiceEvent magic = new ApplicationServiceEvent(serverTopic,sid,id.systemId(),applicationId,tag,_payload);
                magic.stub(id.stub());
                magic.ticket(id.ticket());
                magic.trackId(id.oid());
                magic.action(action!=null?action:"");
                magic.routingNumber(id.routingNumber());
                magic.destination(eventService.routingKey(id.systemId(),tag).route());//use tag with systemId or instanceId partition to route event to target service endpoint
                magic.streaming(exchange.streaming());
                this.eventService.publish(magic);
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
        this.accessIndexService = accessIndexService;
        this.bucket = bucket;
    }
    public void onCheck(){
        //log.warn("Total active session ["+_hex.size()+"] on ["+name()+"]");
    }
}
