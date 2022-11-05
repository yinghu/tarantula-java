package com.tarantula.cci.http;

import com.icodesoftware.service.EndPoint;
import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.RequestHandler;
import com.tarantula.cci.HttpDispatcher;

public class HttpGameServerHandler extends HttpDispatcher {

    public HttpGameServerHandler(MetricsListener metricsListener){
        super(metricsListener);
    }
    public void resource(EndPoint.Resource resource){
        requestHandler = resource.requestHandler(path());
    }
    @Override
    public String path() {
        return RequestHandler.GAME_SERVER_PATH;
    }
}
