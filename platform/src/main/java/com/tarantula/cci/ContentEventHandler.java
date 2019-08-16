package com.tarantula.cci;

import com.tarantula.*;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.event.PendingRequestEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class ContentEventHandler implements RequestHandler {

    private static TarantulaLogger log = JDKLogger.getLogger(ContentEventHandler.class);

    private EventService eventService;
    private TokenValidator auth;
    private String serverTopic;
    private final ConcurrentHashMap<String,OnExchange> _hex = new ConcurrentHashMap<>();



    public ContentEventHandler(){
    }
    public String name(){
        return "/content";
    }
    public void onRequest(OnExchange exchange){
        try{
            String sid = exchange.id();
            _hex.put(sid,exchange);
            String path = exchange.path();
            String token = exchange.query().split("=")[1];
            String clientId = exchange.header(Session.X_REAL_IP)!=null?exchange.header(Session.X_REAL_IP):exchange.remoteAddress().toString();
            OnSession ox = this.auth.validToken(token,clientId);
            String[] plist = path.substring(1).split("/");
            PendingRequestEvent ctn = new PendingRequestEvent(plist[1]);
            ctn.action(plist[2]+Recoverable.PATH_SEPARATOR+plist[3]);
            ctn.systemId(ox.systemId());
            ctn.ticket(ox.ticket());
            ctn.stub(ox.stub());
            ctn.trackId(ox.oid());
            ctn.sessionId(sid);
            ctn.source(this.serverTopic);
            ctn.destination(eventService.routingKey(ox.systemId(),ctn.tag()).route());
            this.eventService.publish(ctn);

        }catch (Exception ex){
            ex.printStackTrace();
            _hex.remove(exchange.id()); //removed cache on any errors
            exchange.onError(ex,"Bad request");
        }
    }
    @Override
    public void start() throws Exception {
        this.serverTopic = UUID.randomUUID().toString();
        this.eventService.registerEventListener(this.serverTopic,this);
        log.info("Content handler started");
    }

    @Override
    public void shutdown() throws Exception {

    }
    @Override
    public void setup(TokenValidator tokenValidator,EventService eventService,AccessIndexService accessIndexService,String bucket) {
        this.auth = tokenValidator;
        this.eventService = eventService;
    }
    public boolean onEvent(Event event){
       OnExchange hx = this._hex.get(event.sessionId());
       if(hx!=null){
           if(hx.onEvent(event)){ //remove on true marked as closed connect or session
               _hex.remove(event.sessionId());
           }
       }
       else{
           log.warn(event.toString()+" unexpected removed");
       }
       return true;
    }
    public void onCheck(){
        log.warn("Total active session ["+_hex.size()+"] on ["+name()+"]");
    }
}
