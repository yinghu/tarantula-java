package com.tarantula.platform.service.metrics;


import com.icodesoftware.service.Metrics;
import com.icodesoftware.service.ServiceContext;

public class AccessMetrics extends AbstractMetrics{

    public final static String ACCESS_USER_CREATION_COUNT = "userCreationCount";
    public final static String ACCESS_SUBSCRIPTION_COUNT = "subscriptionCount";


    @Override
    void _setup(ServiceContext serviceContext) {
        this.name = Metrics.ACCESS;
        this.logger = serviceContext.logger(AccessMetrics.class);
        registerCategory(ACCESS_USER_CREATION_COUNT);
        registerCategory(ACCESS_SUBSCRIPTION_COUNT);
        this.dataStore = serviceContext.dataStore("access_metrics",serviceContext.partitionNumber());
    }
}
