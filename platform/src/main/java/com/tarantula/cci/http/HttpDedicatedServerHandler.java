package com.tarantula.cci.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.tarantula.cci.RequestHandler;

import java.io.IOException;
import java.util.UUID;


public class HttpDedicatedServerHandler implements HttpHandler {

    private RequestHandler pushEventHandler;

    public HttpDedicatedServerHandler(RequestHandler pushEventHandler){
        this.pushEventHandler = pushEventHandler;
	}
	public void handle(HttpExchange hex) throws IOException {
        String id = hex.getRequestHeaders().getFirst("Session-id");
        if(id==null){
            id = UUID.randomUUID().toString();
        }
        HttpSession httpSession = new HttpSession(id,hex);
        httpSession.parse();
        this.pushEventHandler.onRequest(httpSession);
    }
}
