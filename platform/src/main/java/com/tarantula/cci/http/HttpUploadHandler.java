package com.tarantula.cci.http;

import com.sun.net.httpserver.HttpExchange;
import com.tarantula.cci.HttpDispatcher;
import com.tarantula.platform.service.EndPoint;

import java.io.IOException;

/**
 * Created by yinghu lu on 11/9/19.
 */
public class HttpUploadHandler extends HttpDispatcher {

    @Override
    public void resource(EndPoint.Resource resource) {
        requestHandler = resource.requestHandler(path());
    }

    @Override
    public String path() {
        return "/upload";
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        HttpUploadSession httpUploadSession = new HttpUploadSession(httpExchange);
        requestHandler.onRequest(httpUploadSession);
    }
}
