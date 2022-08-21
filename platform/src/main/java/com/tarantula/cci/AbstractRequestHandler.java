package com.tarantula.cci;

import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.RequestHandler;

abstract public class AbstractRequestHandler implements RequestHandler {


    protected MetricsListener metricsListener;


    public AbstractRequestHandler(){
        metricsListener = (m,v)->{};
    }

    public void registerMetricsListener(MetricsListener metricsListener){
        this.metricsListener = metricsListener;
    }

}
