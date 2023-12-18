package com.tarantula.cci.http;

import com.icodesoftware.Session;
import com.icodesoftware.service.EndPoint;
import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.RequestHandler;
import com.sun.net.httpserver.HttpExchange;
import com.tarantula.cci.HttpDispatcher;

import java.io.IOException;

public class HttpHealthCheckHandler extends HttpDispatcher {

    private static final String METRICS_HEADER = "httpHealthCount";
    private byte[] hc = "hc".getBytes();

    public HttpHealthCheckHandler(MetricsListener metricsListener){
        super(metricsListener);
    }

    @Override
    public void resource(EndPoint.Resource resource) {

    }

    @Override
    public String path() {
        return RequestHandler.HEALTH_CHECK_PATH;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try(httpExchange) {
            httpExchange.getResponseHeaders().set(Session.HTTP_CONTENT_TYPE, "text/html");
            httpExchange.sendResponseHeaders(200, 2);
            httpExchange.getResponseBody().write(hc);
        }
        finally {
            metricsListener.onUpdated(METRICS_HEADER,1);
        }
    }
}
