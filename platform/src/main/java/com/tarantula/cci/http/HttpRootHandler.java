package com.tarantula.cci.http;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.tarantula.cci.RequestHandler;
import java.io.IOException;

public class HttpRootHandler extends RequestParser implements HttpHandler {
    private final RequestHandler resourceEventHandler;

    public HttpRootHandler(RequestHandler eventHandler){
        this.resourceEventHandler = eventHandler;
    }
    public void handle(HttpExchange hex) throws IOException {
        HttpSession hs = new HttpSession("id",hex);
        this.resourceEventHandler.onRequest(hs);
    }

}
