package com.tarantula.platform.service.metrics;


import com.icodesoftware.Distributable;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.Metrics;
import com.icodesoftware.service.ServiceContext;

public class SystemMetrics extends AbstractMetrics{

    public final static String SYSTEM_USER_CREATION_COUNT = "systemUserCreationCount";
    public final static String SYSTEM_ACCOUNT_CREATION_COUNT = "systemAccountCreationCount";
    public final static String SYSTEM_SUBSCRIPTION_COUNT = "systemSubscriptionCount";

    @Override
    void _setup(ServiceContext serviceContext) {
        this.name = Metrics.SYSTEM;
        this.logger = JDKLogger.getLogger(SystemMetrics.class);
        this.dataStore = serviceContext.dataStore(Distributable.LOCAL_SCOPE,"tarantula_system_metrics");
    }
}