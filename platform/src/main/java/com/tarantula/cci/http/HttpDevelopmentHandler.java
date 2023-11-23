package com.tarantula.cci.http;

import com.icodesoftware.service.EndPoint;
import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.RequestHandler;
import com.sun.net.httpserver.HttpExchange;
import com.tarantula.cci.HttpDispatcher;

import java.io.IOException;

public class HttpDevelopmentHandler extends HttpDispatcher {

    public HttpDevelopmentHandler(MetricsListener metricsListener){
        super(metricsListener);
    }
    @Override
    public void resource(EndPoint.Resource resource) {
        requestHandler = resource.requestHandler(path());
    }

    @Override
    public String path() {
        return RequestHandler.DEVELOPMENT_PATH;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        HttpUploadSession httpUploadSession = new HttpUploadSession(httpExchange);
        try{
            requestHandler.onRequest(httpUploadSession);
        }catch (Exception ex){
            httpUploadSession.onError(ex,ex.getMessage());
        }
    }
}
