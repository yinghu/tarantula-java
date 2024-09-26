package com.tarantula.platform.service.metrics;

import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ServiceContext;


public class AccessMonitor extends MetricsMonitor {

    private TarantulaLogger logger = JDKLogger.getLogger(AccessMonitor.class);

    public static final String NAME = "AccessMonitor";

    @Override
    public String name() {
        return NAME;
    }
    @Override
    public void setup(ServiceContext serviceContext){
        metrics = serviceContext.metrics(AccessMetrics.ACCESS);
        logger.warn("Starting access monitor");
    }
}
