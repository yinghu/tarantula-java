package com.tarantula.cci.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.tarantula.Session;

import java.io.IOException;

/**
 * Created by yinghu lu on 6/22/2018.
 */
public class HttpHealthCheckHandler implements HttpHandler {

    private byte[] hc = "hc".getBytes();

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        httpExchange.getResponseHeaders().set(Session.HTTP_CONTENT_TYPE,"text/html");
        httpExchange.sendResponseHeaders(200,2);
        httpExchange.getResponseBody().write(hc);
        httpExchange.close();
    }
}
