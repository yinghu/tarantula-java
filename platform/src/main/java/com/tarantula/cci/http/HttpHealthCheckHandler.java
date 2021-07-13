package com.tarantula.cci.http;

import com.icodesoftware.Session;
import com.sun.net.httpserver.HttpExchange;
import com.tarantula.cci.HttpDispatcher;
import com.tarantula.cci.RequestHandler;
import com.tarantula.platform.service.EndPoint;

import java.io.IOException;

public class HttpHealthCheckHandler extends HttpDispatcher {

    private byte[] hc = "hc".getBytes();

    @Override
    public void resource(EndPoint.Resource resource) {

    }

    @Override
    public String path() {
        return RequestHandler.HEALTH_CHECK_PATH;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        httpExchange.getResponseHeaders().set(Session.HTTP_CONTENT_TYPE,"text/html");
        httpExchange.sendResponseHeaders(200,2);
        httpExchange.getResponseBody().write(hc);
        httpExchange.close();
    }
}
