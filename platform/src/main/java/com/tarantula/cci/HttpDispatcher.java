package com.tarantula.cci;

import com.icodesoftware.service.EndPoint;
import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.RequestHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.tarantula.cci.http.HttpSession;
import com.tarantula.platform.service.metrics.PerformanceMetrics;

import java.io.IOException;
import java.util.UUID;

abstract public class HttpDispatcher implements HttpHandler {
    protected RequestHandler requestHandler;
    private MetricsListener metricsListener;

    abstract public void resource(EndPoint.Resource resource);
    abstract public String path();

    public HttpDispatcher(MetricsListener metricsListener){
        this.metricsListener = metricsListener;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String id = httpExchange.getRequestHeaders().getFirst("Session-id");
        if(id==null){
            id = UUID.randomUUID().toString();
        }
        HttpSession exchange = new HttpSession(id,httpExchange);
        exchange.parse();
        try{
            requestHandler.onRequest(exchange);
            metricsListener.onUpdated(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT,1);
        }catch (Exception ex){
            //ex.printStackTrace();
            metricsListener.onUpdated(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_ERROR_COUNT,1);
            exchange.onError(ex,ex.getMessage());
        }
    }
}
