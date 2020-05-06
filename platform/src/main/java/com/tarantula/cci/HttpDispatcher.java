package com.tarantula.cci;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.tarantula.cci.http.HttpSession;
import com.tarantula.platform.service.EndPoint;

import java.io.IOException;
import java.util.UUID;

abstract public class HttpDispatcher implements HttpHandler {
    protected RequestHandler requestHandler;

    abstract public void resource(EndPoint.Resource resource);
    abstract public String path();

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String id = httpExchange.getRequestHeaders().getFirst("Session-id");
        if(id==null){
            id = UUID.randomUUID().toString();
        }
        HttpSession exchange = new HttpSession(id,httpExchange);
        exchange.parse();
        requestHandler.onRequest(exchange);
    }
}
