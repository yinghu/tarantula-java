package com.tarantula.cci.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.tarantula.cci.RequestHandler;

import java.io.IOException;
import java.util.UUID;


public class HttpApplicationHandler implements HttpHandler {

    private RequestHandler applicationEventHandler;

    public HttpApplicationHandler(RequestHandler applicationEventHandler){
        this.applicationEventHandler = applicationEventHandler;
	}
	public void handle(HttpExchange hex) throws IOException {
        /**
        if(hex.getRequestMethod().equalsIgnoreCase("OPTIONS")){
            hex.getResponseHeaders().set("Access-Control-Allow-Headers","Tarantula-token,Tarantula-application-id,Tarantula-instance-id,Tarantula-action,Tarantula-magic-key,Tarantula-payload-size,Forwarding-application-id,Forwarding-magic-key");
            hex.sendResponseHeaders(200,0);
            hex.close();
            return;
        }**/
        String id = hex.getRequestHeaders().getFirst("Session-id");
        if(id==null){
            id = UUID.randomUUID().toString();
        }
        HttpSession httpSession = new HttpSession(id,hex);
        httpSession.parse();
        this.applicationEventHandler.onRequest(httpSession);
    }
}
