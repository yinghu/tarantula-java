package com.tarantula.cci.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.tarantula.cci.RequestHandler;
import java.io.IOException;

/**
 * Created by yinghu lu on 11/9/19.
 */
public class HttpUploadHandler implements HttpHandler {

    private RequestHandler uploadEventHandler;

    public HttpUploadHandler(RequestHandler uploadEventHandler){
        this.uploadEventHandler = uploadEventHandler;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        HttpUploadSession httpUploadSession = new HttpUploadSession(httpExchange);
        uploadEventHandler.onRequest(httpUploadSession);
    }
}
