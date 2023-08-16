package com.tarantula.cci;

import com.icodesoftware.Event;

import com.icodesoftware.OnSession;
import com.icodesoftware.RoutingKey;
import com.icodesoftware.service.*;
import com.tarantula.platform.event.PermissionCheckEvent;


import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

abstract public class AbstractRequestHandler implements RequestHandler {


    protected MetricsListener metricsListener;

    protected boolean onEvent;
    protected ConcurrentHashMap<String, OnExchange> eMap;
    protected EventService eventService;
    protected TokenValidatorProvider tokenValidator;
    protected String serviceTopic;
    public AbstractRequestHandler(boolean onEvent){
        metricsListener = (m,v)->{};
        this.onEvent = onEvent;
        if(this.onEvent){
            eMap = new ConcurrentHashMap<>();
        }
    }

    public void registerMetricsListener(MetricsListener metricsListener){
        this.metricsListener = metricsListener;
    }

    public void onRequest(OnExchange exchange) throws Exception{
           if(!onEvent) return;
           eMap.put(exchange.id(),exchange);
    }
    @Override
    public void shutdown() throws Exception {

    }

    @Override
    public void start() throws Exception {
        if(!onEvent) return;
        serviceTopic = UUID.randomUUID().toString();
        eventService.registerEventListener(serviceTopic,this);
    }

    public void setup(ServiceContext tcx){
        this.tokenValidator = (TokenValidatorProvider) tcx.serviceProvider(TokenValidatorProvider.NAME);
        if(!onEvent) return;
        this.eventService = tcx.eventService();
    }
    @Override
    public boolean onEvent(Event event) {
        if(!onEvent) return false;
        OnExchange hx = this.eMap.remove(event.sessionId());
        if(hx==null) return false;
        hx.onEvent(event);
        return true;
    }
    public void onCheck(){}
    public boolean deployable(){return true;}

    protected void checkPermission(OnSession id,String token,String sessionId,String tag){
        PermissionCheckEvent actionEvent = new PermissionCheckEvent(this.serviceTopic,sessionId);
        RoutingKey routingKey = eventService.routingKey(id.systemId(),tag);
        actionEvent.systemId(id.systemId());
        actionEvent.stub(id.stub());
        actionEvent.ticket(id.ticket());
        actionEvent.token(token);
        actionEvent.action("onCheckPermission");
        actionEvent.routingNumber(routingKey.routingNumber());
        actionEvent.destination(routingKey.route());
        this.eventService.publish(actionEvent);
    }
}
