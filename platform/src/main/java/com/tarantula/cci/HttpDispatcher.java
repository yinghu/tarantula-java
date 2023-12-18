package com.tarantula.cci;

import com.icodesoftware.service.EndPoint;
import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.RequestHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.tarantula.cci.http.HttpSession;


import java.io.IOException;

abstract public class HttpDispatcher implements HttpHandler {

    protected RequestHandler requestHandler;
    protected MetricsListener metricsListener;

    protected final static String METRICS_ERROR_CATEGORY = "httpErrorCount";

    abstract public void resource(EndPoint.Resource resource);
    abstract public String path();

    public HttpDispatcher(MetricsListener metricsListener){
        this.metricsListener = metricsListener;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        HttpSession exchange = new HttpSession(requestHandler.snowflakeId(),httpExchange);
        exchange.parse();
        try{
            requestHandler.onRequest(exchange);
            metricsListener.onUpdated(requestHandler.metricsCategory(),1);
        }catch (Exception ex){
            metricsListener.onUpdated(METRICS_ERROR_CATEGORY,1);
            exchange.onError(ex,ex.getMessage());
        }
    }
}
