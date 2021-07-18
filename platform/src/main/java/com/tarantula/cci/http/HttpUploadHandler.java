package com.tarantula.cci.http;

import com.sun.net.httpserver.HttpExchange;
import com.tarantula.cci.HttpDispatcher;
import com.tarantula.cci.RequestHandler;
import com.tarantula.platform.service.EndPoint;

import java.io.IOException;

public class HttpUploadHandler extends HttpDispatcher {

    @Override
    public void resource(EndPoint.Resource resource) {
        requestHandler = resource.requestHandler(path());
    }

    @Override
    public String path() {
        return RequestHandler.UPLOAD_PATH;
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
