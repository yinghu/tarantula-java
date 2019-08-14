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
        if(httpExchange.getRequestMethod().equalsIgnoreCase("OPTIONS")){
            httpExchange.getResponseHeaders().set("Access-Control-Allow-Headers","Tarantula-token,Tarantula-application-id,Tarantula-instance-id,Tarantula-action,Tarantula-magic-key,Tarantula-payload-size,Forwarding-application-id,Forwarding-magic-key");
            httpExchange.sendResponseHeaders(200,0);
            httpExchange.close();
            return;
        }
        String id = httpExchange.getRequestHeaders().getFirst("Session-id");
        if(id==null){
            id = UUID.randomUUID().toString();
        }
        HttpSession exchange = new HttpSession(id,httpExchange);
        exchange.parse();
        userEventHandler.onRequest(exchange);
    }

}
