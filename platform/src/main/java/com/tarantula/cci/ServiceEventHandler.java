package com.tarantula.cci;

import com.tarantula.*;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.OnSessionTrack;
import com.tarantula.platform.event.ServiceActionEvent;
import com.tarantula.platform.service.AccessIndexService;
import com.tarantula.platform.service.DeploymentServiceProvider;
import com.tarantula.platform.service.ServiceContext;
import com.tarantula.platform.service.TokenValidatorProvider;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class ServiceEventHandler implements RequestHandler {

	private static final JDKLogger log = JDKLogger.getLogger(ServiceEventHandler.class);

    private EventService eventService;
    private TokenValidator auth;
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
                String instanceId = exchange.header(Session.TARANTULA_INSTANCE_ID);//instance Id
                String name = exchange.header(Session.TARANTULA_NAME);//key name

                byte[]  _payload = exchange.payload();
                String sid = exchange.id();
                this._hex.put(sid,exchange);
                if(path.startsWith("/service/action")){
                    OnSession id = new OnSessionTrack();//place holder for public access applications
                    RoutingKey routingKey = eventService.routingKey(this.bucket+"/"+sid,tag);
                    if((token!=null)&&(!token.equals("undefined"))){
                        id = auth.validateToken(token);//first entry point check
                        routingKey = eventService.routingKey(id.systemId(),tag);
                    }
                    ServiceActionEvent actionEvent = new ServiceActionEvent(this.serverTopic,sid,_payload);
                    actionEvent.systemId(id.systemId());
                    actionEvent.stub(id.stub());
                    actionEvent.ticket(id.ticket());
                    actionEvent.trackId(id.oid());
                    actionEvent.action(action!=null?action:path);
                    actionEvent.routingNumber(routingKey.routingNumber());
                    actionEvent.destination(routingKey.route());
                    actionEvent.streaming(exchange.streaming());
                    actionEvent.instanceId(instanceId);
                    actionEvent.name(name);
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
    public void setup(ServiceContext tcx){
        this.eventService = tcx.eventService(Distributable.INTEGRATION_SCOPE);
        TokenValidatorProvider tp = (TokenValidatorProvider) tcx.serviceProvider(TokenValidatorProvider.NAME);
        this.auth = tp.tokenValidator();
        this.bucket = tcx.bucket();
    }
    public void onCheck(){
        //log.warn("Total active session ["+_hex.size()+"] on ["+name()+"]");
    }
}
