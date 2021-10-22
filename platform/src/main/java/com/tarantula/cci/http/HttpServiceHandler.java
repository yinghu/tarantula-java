package com.tarantula.cci.http;

import com.icodesoftware.service.EndPoint;
import com.icodesoftware.service.RequestHandler;
import com.tarantula.cci.HttpDispatcher;


public class HttpServiceHandler extends HttpDispatcher {


    @Override
    public void resource(EndPoint.Resource resource) {
        requestHandler = resource.requestHandler(path());
    }

    @Override
    public String path() {
        return RequestHandler.SERVICE_PATH;
    }
}
