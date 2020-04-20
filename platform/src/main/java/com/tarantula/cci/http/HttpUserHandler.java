package com.tarantula.cci.http;

import com.tarantula.cci.RequestHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.UUID;

public class HttpUserHandler implements HttpHandler {


    private RequestHandler userEventHandler;

    public HttpUserHandler(RequestHandler userEventHandler){
        this.userEventHandler = userEventHandler;
    }
    public void handle(HttpExchange httpExchange) throws IOException {
        String id = httpExchange.getRequestHeaders().getFirst("Session-id");
        if(id==null){
            id = UUID.randomUUID().toString();
        }
        HttpSession exchange = new HttpSession(id,httpExchange);
        exchange.parse();
        userEventHandler.onRequest(exchange);
    }

}
