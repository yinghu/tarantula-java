package com.icodesoftware.admin;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class AdminServiceProvider implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(200,5);
        exchange.getResponseBody().write("hello".getBytes());
        exchange.close();
    }
}
