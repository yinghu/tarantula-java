package com.tarantula.cci.http;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.tarantula.cci.RequestHandler;

import java.io.IOException;
import java.util.UUID;


public class HttpContentHandler implements HttpHandler {

    private final RequestHandler contentEventHandler;

    public HttpContentHandler(RequestHandler contentEventHandler){
        this.contentEventHandler = contentEventHandler;
    }
    public void handle(HttpExchange hex) throws IOException {
        String id = hex.getRequestHeaders().getFirst("Session-id");
        if(id==null){
            id = UUID.randomUUID().toString();
        }
        HttpBatchSession httpSession = new HttpBatchSession(id,hex);
        httpSession.parse();
        this.contentEventHandler.onRequest(httpSession);
    }
}
