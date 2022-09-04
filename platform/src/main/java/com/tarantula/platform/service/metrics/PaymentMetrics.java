package com.tarantula.platform.service.metrics;


import com.icodesoftware.service.Metrics;
import com.icodesoftware.service.ServiceContext;

public class PaymentMetrics extends AbstractMetrics{


    @Override
    void _setup(ServiceContext serviceContext) {
        this.name = Metrics.PAYMENT;
        this.logger = serviceContext.logger(PaymentMetrics.class);
        this.dataStore = serviceContext.dataStore("payment_metrics",serviceContext.partitionNumber());
    }
}
