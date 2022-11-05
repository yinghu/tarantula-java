package com.tarantula.cci.http;
import com.icodesoftware.service.EndPoint;
import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.RequestHandler;
import com.tarantula.cci.HttpDispatcher;


public class HttpResourceHandler extends HttpDispatcher {


    public HttpResourceHandler(MetricsListener metricsListener){
        super(metricsListener);
    }
    @Override
    public void resource(EndPoint.Resource resource) {
        requestHandler = resource.requestHandler(path()) ;
    }

    @Override
    public String path() {
        return RequestHandler.RESOURCE_PATH;
    }


}
