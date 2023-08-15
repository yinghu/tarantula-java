package com.tarantula.cci;

import com.icodesoftware.Event;
import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.OnExchange;
import com.icodesoftware.service.RequestHandler;

import java.util.concurrent.ConcurrentHashMap;

abstract public class AbstractRequestHandler implements RequestHandler {


    protected MetricsListener metricsListener;

    protected boolean onEvent;
    protected ConcurrentHashMap<String, OnExchange> eMap;
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
    public boolean onEvent(Event event) {
        OnExchange hx = this.eMap.remove(event.sessionId());
        if(hx!=null){
            //logger.warn("user event ["+event.sessionId()+"]");
            hx.onEvent(event);
        }
        return true;
    }
}
