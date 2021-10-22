package com.tarantula.cci.http;

import com.icodesoftware.service.EndPoint;
import com.icodesoftware.service.RequestHandler;
import com.sun.net.httpserver.HttpExchange;
import com.tarantula.cci.HttpDispatcher;

import java.io.IOException;

public class HttpRootHandler extends HttpDispatcher {
    @Override
    public void resource(EndPoint.Resource resource) {
        requestHandler = resource.requestHandler(path());
    }

    @Override
    public String path() {
        return RequestHandler.ROOT_PATH;
    }

    public void handle(HttpExchange hex) throws IOException {
        HttpSession exchange = new HttpSession("id",hex);
        exchange.parse();
        try{
            requestHandler.onRequest(exchange);
        }catch (Exception ex){
            exchange.onError(ex,ex.getMessage());
        }
    }

}
