package com.tarantula.cci.http;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.tarantula.cci.RequestHandler;

import java.io.IOException;
import java.util.UUID;


public class HttpResourceHandler implements HttpHandler {

    private final RequestHandler resourceEventHandler;

    public HttpResourceHandler(RequestHandler resourceEventHandler){
        this.resourceEventHandler = resourceEventHandler;
    }
    public void handle(HttpExchange hex) throws IOException {
        String id = hex.getRequestHeaders().getFirst("Session-id");
        if(id==null){
            id = UUID.randomUUID().toString();
        }
        HttpSession httpSession = new HttpSession(id,hex);
        httpSession.parse();
        this.resourceEventHandler.onRequest(httpSession);
    }
}
