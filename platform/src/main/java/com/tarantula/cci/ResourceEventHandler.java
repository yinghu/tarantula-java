package com.tarantula.cci;

import com.tarantula.*;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.event.IndexEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class ResourceEventHandler implements RequestHandler {

    private static TarantulaLogger log = JDKLogger.getLogger(ResourceEventHandler.class);

    private String bucket;
    private EventService eventService;


    private String serverTopic;
    private final ConcurrentHashMap<String,OnExchange> _hex = new ConcurrentHashMap<>();



    public ResourceEventHandler(){
    }
    public String name(){
        return "/resource";
    }
    public void onRequest(OnExchange exchange){
        try{
            String sid = exchange.id();
            _hex.put(sid,exchange);
            String path = exchange.path();
            RoutingKey routingKey = eventService.routingKey((this.bucket+"/"+sid),"index/lobby");
            IndexEvent indexEvent = new IndexEvent(this.serverTopic,sid);
            indexEvent.destination(routingKey.route());
            indexEvent.action("resource");
            indexEvent.viewId = path.substring(1);
            this.eventService.publish(indexEvent);

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
    public void setup(TokenValidator tokenValidator,EventService eventService,AccessIndexService accessIndexService,String bucket,DeploymentServiceProvider deploymentServiceProvider) {
        this.eventService = eventService;
        this.bucket = bucket;
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
        //log.warn("Total active session ["+_hex.size()+"] on ["+name()+"]");
    }
}
