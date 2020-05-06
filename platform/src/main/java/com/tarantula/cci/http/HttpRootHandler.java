package com.tarantula.cci.http;


import com.sun.net.httpserver.HttpExchange;
import com.tarantula.cci.HttpDispatcher;
import com.tarantula.platform.service.EndPoint;

import java.io.IOException;

public class HttpRootHandler extends HttpDispatcher {
    @Override
    public void resource(EndPoint.Resource resource) {
        requestHandler = resource.requestHandler(path());
    }

    @Override
    public String path() {
        return "/";
    }

    public void handle(HttpExchange hex) throws IOException {
        HttpSession hs = new HttpSession("id",hex);
        hs.parse();
        this.requestHandler.onRequest(hs);
    }

}
